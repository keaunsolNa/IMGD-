package com.nks.imgd.service.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.nks.imgd.component.util.commonMethod.CommonMethod;
import com.nks.imgd.dto.data.MakeDirDTO;
import com.nks.imgd.dto.data.MakeFileDTO;
import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.mapper.file.FileTableMapper;
import com.nks.imgd.service.user.UserProfilePort;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nks
 * @apiNote File 관련 작업을 하는 서비스
 * 			그룹 권한에 연계 되어 접근 가능 범위를 지정 한다.
 * 	        파일 압축 알고리즘 고려
 * <p>
 * 환경 변수에 지정된 RootPath 를 시작점으로,
 * directory 루트 구조는 다음과 같다
 * RootPath
 *      └──────Group
 *          └────── 각 그룹별 directory (groupId_groupNm, 1_테스트 그룹)
 *              └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *              └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                  └────── 개별 IMG 파일
 *                  └────── 개별 IMG 파일
 *                      └────── 그룹 내 개별 directory (추억, 국내 여행, 해외 여행ㆍㆍㆍㆍ,  ETC)
 *                      └────── 개별 IMG 파일
 *                      └────── 개별 IMG 파일
 *          └────── 각 그룹별 directory (groupId_groupNm, 2_테스트그룹2)
 *      └──────Personal
 *          └────── 각 계정별 directory (user_id)
 *              └────── 계정별 프로필 사진, 필요시 HISTORY 기능 추가.
 * </p>
 */
@Service
@Slf4j
public class FileService {

	private final FileTableMapper fileTableMapper;
	private final UserProfilePort userProfilePort;
	private final Path rootPath = Paths.get(resolveRootFromEnv()).toAbsolutePath().normalize();
	private final CommonMethod commonMethod = new CommonMethod();

	/** 운영 기본: 시스템 프로퍼티/환경변수에서 루트 경로 로드 */
	@Autowired
	public FileService(FileTableMapper fileTableMapper, UserProfilePort userProfilePort) {
		this(fileTableMapper, resolveRootFromEnv(), userProfilePort);
	}

	/** 테스트/특수 상황용: 직접 루트 경로 주입 */
	public FileService(FileTableMapper fileTableMapper, String root, UserProfilePort userProfilePort) {
		this.fileTableMapper = fileTableMapper;
		this.userProfilePort = userProfilePort;
		try {
			Files.createDirectories(this.rootPath); // 존재 보장
		} catch (IOException e) {
			throw new IllegalStateException("Cannot create root directory: " + this.rootPath, e);
		}
		log.info("IMGD root path = {}", this.rootPath);
	}

	public List<FileTableDTO> findFileAndDirectory(@Param("parentId") Long parentId, @Param("groupId")  Long groupId) {
		return postProcessingFileTables(fileTableMapper.findFileAndDirectory(parentId, groupId));
	}
    /**
	 * 그룹 생성시 그룹의 루트가 될 폴더를 만든다.
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
	 *
     * @param dto directory 생성할 그룹
	 * @return 생성된 폴더의 정보
     */
	@Transactional(rollbackFor = Exception.class)
    public ResponseEntity<FileTableDTO> makeGroupDir(GroupTableDTO dto)
    {

		if (fileTableMapper.makeGroupDir(dto) != 1) return ResponseEntity.badRequest().build();

		FileTableDTO fileDTO = fileTableMapper.selectFileIdByFileOrgNmInDirCase(dto);

		if (null == fileDTO  || null == fileDTO.getFileId()) {
			return ResponseEntity.badRequest().build();
		}

		return returnResultWhenTransaction(createDirectoriesOrThrow(makePathByFileIdAndFileNm(fileDTO.getFileId())),
			() -> selectFileById(fileDTO.getFileId()));

	}

	/**
	 * 하위 디렉터리 생성
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
	 *
	 * @param req 폴더 정보가 담긴 DTO
	 * @return 생성된 폴더 정보
	 */
	@Transactional(rollbackFor = Exception.class)
    public ResponseEntity<List<FileTableDTO>> makeDir(MakeDirDTO req)
    {

		System.out.println("REQ : " + req);
		if (fileTableMapper.makeDir(req) != 1) return ResponseEntity.badRequest().build();

		return returnResultWhenTransaction(createDirectoriesOrThrow(Path.of(makePathByFileIdAndFileNm(req.getParentId()) + "\\" + req.getDirNm())),
			() -> findFileAndDirectory(req.getParentId(), req.getGroupId()));
    }

	/**
	 * 사용자의 Profile Img를 변경한다.
	 * @param dto 저장할 파일에 관한 정보가 담긴 dto
	 * @param originalFile 변경할 파일
	 * @return insert 후 결과값
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<UserTableDTO> makeUserProfileImg(MakeFileDTO dto, File originalFile)
	{
		String fileNm = UUID.randomUUID().toString();

		FileTableDTO fileDTO = new FileTableDTO();
		fileDTO.setFileNm(fileNm);
		fileDTO.setFileOrgNm(dto.getFileName());

		// ✅ 파일 테이블에 ROW 생성
		int result = fileTableMapper.makeUserProfileImg(fileDTO, dto.getUserId());

		if (result != 1) return ResponseEntity.badRequest().build();

		// 성공 했다면 user 정보 변경한다.
		long fileId = fileDTO.getFileId();

		// ✅ 유저 정보 확인
		UserTableDTO userTableDTO = userProfilePort.findUserById(dto.getUserId());

		if (null == userTableDTO) return ResponseEntity.badRequest().build();

		// 기존 유저 정보에 사진이 있다면 해당 파일을 삭제한다.
		if (null != userTableDTO.getPictureId())
		{
			try {

				boolean resultDeleteFile = deleteFileById(userTableDTO.getPictureId());

				if (resultDeleteFile) log.info("Deleted file ID: {}", fileId);
				else log.info("Failed to delete file ID: {}", fileId);

			} catch (Exception e) {
				// 실패해도 신규 사용에는 영향 없음
				log.warn("Best-effort delete of old profile image failed. oldPictureId={}", userTableDTO.getPictureId(), e);
			}
		}

		// ✅ 유저 정보(사진 ID) 변경
		userTableDTO.setPictureId(fileId);
		int userResult = userProfilePort.updatePictureId(userTableDTO.getUserId(), fileId);

		if (userResult != 1) return ResponseEntity.badRequest().build();
		Path targetNoExt = makePathByFileIdAndFileNm(3L);

		// ✅ 파일 Webp 형태로 변환 및 저장
		if (null == convertToWebp(targetNoExt, originalFile, fileDTO.getFileNm())) return ResponseEntity.badRequest().build();
		else return ResponseEntity.ok(userTableDTO);

	}

	/**
	 * @param folderId 파일이 생성될 부모 폴더 ID
	 * @param userId 파일을 생성하는 그룹의 MST_USER_ID
	 * @param groupId 파일이 생성될 그룹 ID
	 * @param fileOrgNm 생성할 파일 원본 이름
	 * @param originalFile 생성할 파일
	 * @return int 결과 값
	 * 파일 생성
	 * TODO : 그룹 계정의 과금 권한에 따른 파일 용량/압축률/최대크기등 설정
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<FileTableDTO> makeFile(Long folderId, String userId, Long groupId, String fileOrgNm, File originalFile)
	{

		MakeFileDTO dto =  new MakeFileDTO();
		String fileNm = UUID.randomUUID().toString();
		String path = selectFileNmByDirId(folderId);


		dto.setFileNm(fileNm);
		dto.setFileOrgNm(fileOrgNm);
		dto.setPath(path);
		dto.setFolderId(folderId);
		dto.setGroupId(groupId);
		dto.setUserId(userId);
		System.out.println("DTO : " + dto);

		if (fileTableMapper.makeFile(dto) != 1)
			return ResponseEntity.badRequest().build();

		Path targetNoExt = makePathByFileIdAndFileNm(folderId);

		if (null == convertToWebp(targetNoExt, originalFile, fileNm)) return ResponseEntity.badRequest().build();
		else return ResponseEntity.ok(selectFileById(dto.getFileId()));

	}


	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 파일 삭제 메서드
	 * @param fileId 삭제할 파일 아이디
	 * @return 삭제 성공/실패
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteFileById(Long fileId) {
		// 1) 파일 메타 조회
		FileTableDTO row = selectFileById(fileId);
		if (null == row) {
			log.warn("deleteFileById: file row not found, fileId={}", fileId);
			return false;
		}

		// 디렉터리인 경우 별도 API로 처리하도록 가드
		if ("DIR".equalsIgnoreCase(row.getType())) {
			throw new IllegalArgumentException("Directory deletion is not supported here. Use deleteDirRecursively: " + fileId);
		}

		// 2) 물리 경로 계산
		//    파일은 "부모 디렉터리 경로" + "fileNm + 확장자"
		Path parentDir = makePathByFileIdAndFileNm(row.getParentId());
		// 기본 저장 포맷이 webp라면:
		Path main = parentDir.resolve(row.getFileNm() + ".webp");

		boolean deletedAny = deleteQuietly(main);
		// 2-1) 같은 baseName의 파생 파일들(.jpg, .png, *_thumb.webp 등)도 함께 제거
		deletedAny |= deleteSiblingsByBaseName(parentDir, row.getFileNm());

		if (deletedAny) log.info("deleted physical file ID: {}", fileId);
		else log.info("failed deleted physical file ID: {}", fileId);

		// 3) DB Row 삭제 (파일 삭제가 일부 실패해도 DB는 맞춰주는 편/또는 soft delete로 전환 가능)
		int db = fileTableMapper.deleteById(fileId);
		if (db != 1) {
			log.warn("deleteFileById: DB row delete failed, fileId={}", fileId);
		}

		// 4) (선택) 빈 디렉터리 정리
		cleanupIfEmpty(parentDir);

		return (db == 1);
	}

	/**
	 * 파일의 ID와 NM으로
	 * @param fileId 파일의 ID
	 * @return 파일의 절대 경로
	 */
	public Path makePathByFileIdAndFileNm(Long fileId)
	{

		String relativeChain = selectRootPath(fileId);
		String parentName = selectFileNmByDirId(fileId);

		return resolveUnderRoot(relativeChain).resolve(sanitizeSegment(parentName));
	}

	/**
	 * 사진 파일을 Webp 형식 파일로 변환한다.
	 * @param path 업로드할 파일의 위치
	 * @param originalFile 업로드할 파일
	 * @return 변환된 파일
	 */
	public File convertToWebp(Path path, File originalFile, String fileNm)
	{

		// 최종 대상: uuid.webp
		Path target = path.resolve(
			fileNm + ".webp"
		);

		System.out.println("target : " + target);
		System.out.println("originalFile : " + originalFile);
		System.out.println("fileNm : " + fileNm);
		createDirectoriesOrThrow(target.getParent());

		try
		{
			ImmutableImage.loader()
				.fromFile(originalFile)
				.output(WebpWriter.DEFAULT, target.toFile()); // 여기서 실제 파일 생성
			return target.toFile();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * FILE_TABLE를 역추적하여 상대 경로 체인을 돌려준다.
	 * @param fileId 대상 파일 ID
	 * @return 상대 경로 체인
	 */
	public String selectRootPath(Long fileId) {

		StringBuilder sb = new StringBuilder();

		Long cur = fileId;

		while (null != cur) {
			FileTableDTO r = fileTableMapper.selectRootPath(cur);
			if (r == null) break;
			if (null != r.getFilePath()) {
				sb.insert(0, "/" + r.getFilePath());
			}
			cur = r.getParentId();
		}
		return sb.toString();

	}

	/**
	 * 경로 아이디로 파일 이름 반환
	 * @param dirId 경로 ID
	 * @return 파일 이름
	 */
	public String selectFileNmByDirId(Long dirId) {

		return fileTableMapper.selectFileNmByDirId(dirId).getFileNm();
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	public FileTableDTO selectFileById(Long fileId) {
		return fileTableMapper.selectFileById(fileId);
	}
	/**
	 * DB가 돌려준 상대/절대 비슷한 문자열을 루트 기준 절대 Path로 정규화.
	 * @param relativeOrAbsolute 대상 문자열
	 * @return 절대경로
	 */
	private Path resolveUnderRoot(String relativeOrAbsolute) {

		if (null == relativeOrAbsolute || relativeOrAbsolute.isBlank()) {
			return rootPath;
		}

		String cleaned = relativeOrAbsolute.replace("\\", "/").replaceFirst("^/+", "");
		return rootPath.resolve(cleaned).normalize();

	}

	/**
	 * 금칙문자 치환: Windows 등에서 문제되는 문자들을 '_'로 대체.
	 * @param name 대상 문자열
	 * @return 치환 문자열
	 */
	private String sanitizeSegment(String name) {
		if (null == name) return "_";
		return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
	}

	/**
	 * 디렉터리 생성(존재 시 통과). 실패 시 일관된 예외로 래핑.
	 * @param dir 대상 디렉터리
	 */
	private int createDirectoriesOrThrow(Path dir) {

		System.out.println("DIR : " + dir);
		try {
			Files.createDirectories(dir);
			return 1;
		} catch (FileAlreadyExistsException e) {
			// 같은 이름의 "파일"이 이미 있는 경우
			throw new IllegalStateException("Path exists but is not a directory: " + dir, e);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create directory: " + dir, e);
		}
	}

	/**
	 * 환경변수 읽어오기
	 * @return 환경 변수 
	 */
	private static String resolveRootFromEnv() {
		// 1) JVM 옵션: -Dimgd.root.path=...
		String v = System.getProperty("imgd.root.path");
		if (null == v || v.isBlank()) {
			// 2) 환경변수(우선): IMGD_ROOT_PATH (예: C:/IMGD)
			v = System.getenv("IMGD_ROOT_PATH");
		}
		if (v == null || v.isBlank()) {
			// 3) 레거시 호환
			v = System.getenv("FILE_ROOT");
		}
		return (null == v || v.isBlank()) ? "C:/IMGD" : v;
	}

	/**
	 * 물리 파일을 삭제한다.
	 * @param p 대상 경로
	 * @return 결과값
	 */
	private boolean deleteQuietly(Path p) {
		try {
			return Files.deleteIfExists(p);
		} catch (IOException e) {
			log.warn("Failed to delete file: {}", p, e);
			return false;
		}
	}

	/**
	 * 유사한 이름의 파일을 삭제한다. (확장자별)
	 * @param dir 대상 경로
	 * @param baseName 대상 이름
	 * @return 결과값
	 */
	private boolean deleteSiblingsByBaseName(Path dir, String baseName) {
		boolean any = false;
		try (var ds = java.nio.file.Files.newDirectoryStream(dir, baseName + "*")) {
			for (Path p : ds) {
				// 이미 지운 메인 파일과 같으면 skip
				any |= deleteQuietly(p);
			}
		} catch (IOException e) {
			log.debug("Skip scanning siblings for {} in {}: {}", baseName, dir, e.toString());
		}
		return any;
	}

	/**
	 * 폴더가 비었을 경우 폴더를 삭제한다.
	 * @param dir 대상 폴더
	 */
	private void cleanupIfEmpty(Path dir) {
		try {
			if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
				Files.delete(dir);
				log.debug("Removed empty dir: {}", dir);
			}
		} catch (IOException e) {
			// 디렉터리 정리는 선택 사항이므로 조용히 패스
			log.debug("Skip cleanup for {}: {}", dir, e.toString());
		}
	}

	/**
	 * 파일 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 * @param files 대상 파일 리스트
	 * @return 후처리 후 대상 파일 리스트
	 */
	public List<FileTableDTO> postProcessingFileTables(List<FileTableDTO> files) {

		for (FileTableDTO file : files) {
			postProcessingFileTable(file);
		}

		return files;
	}

	/**
	 * 파일 반환 시 후처리 진행 한다.
	 * DTM(YYYYMMDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 * @param file 대상 파일
	 * @return 후처리 후 대상 파일
	 */
	public FileTableDTO postProcessingFileTable(FileTableDTO file) {

		if (null == file) return null;
		file.setRegDtm(null != file.getRegDtm() ? commonMethod.translateDate(file.getRegDtm()) : null);
		file.setModDtm(null != file.getModDtm() ? commonMethod.translateDate(file.getModDtm()) : null);

		return file;
	}

	/**
	 * Transaction 결과 값을 반환 한다.
	 *
	 * @param result 결과값
	 * @return 결과값
	 */
	public <T> ResponseEntity<T> returnResultWhenTransaction(int result, Supplier<T> onSuccess) {

		log.info("result, {}", result);
		log.info("onSuccess.get(), {}", onSuccess.get());
		if (result == 1) return ResponseEntity.ok(onSuccess.get());
		else if (result == 0) return ResponseEntity.notFound().build();
		else return ResponseEntity.badRequest().build();
	}

}
