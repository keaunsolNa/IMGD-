package com.nks.imgd.service.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.ibatis.annotations.Param;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.MakeDirDto;
import com.nks.imgd.dto.data.MakeFileDto;
import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.enums.ResponseMsg;
import com.nks.imgd.dto.enums.Role;
import com.nks.imgd.dto.schema.FileTable;
import com.nks.imgd.mapper.file.FileTableMapper;
import com.nks.imgd.service.group.GroupPort;
import com.nks.imgd.service.user.UserProfilePort;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

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
	private final GroupPort groupPort;
	private final Path rootPath = Paths.get(resolveRootFromEnv()).toAbsolutePath().normalize();
	private final CommonMethod commonMethod = new CommonMethod();

	public FileService(FileTableMapper fileTableMapper, UserProfilePort userProfilePort, GroupPort groupPort) {
		this.fileTableMapper = fileTableMapper;
		this.userProfilePort = userProfilePort;
		this.groupPort = groupPort;
		try {
			Files.createDirectories(this.rootPath); // 존재 보장
		} catch (IOException e) {
			throw new IllegalStateException("Cannot create root directory: " + this.rootPath, e);
		}
		log.info("IMGD root path = {}", this.rootPath);
	}

	/**
	 * 해당 위치에 존재 하는 파일 / 폴더를 반환 한다.
	 *
	 * @param parentId 현재 유저가 위차한 폴더의 ID
	 * @param groupId 현재 유저가 위치한 그룹의 ID
	 * @return 해당 위치에 존재 하는 파일 / 폴더 목록
	 */
	public List<FileTable> findFileAndDirectory(@Param("parentId") Long parentId, @Param("groupId") Long groupId) {
		return postProcessingFileTables(fileTableMapper.findFileAndDirectory(parentId, groupId));
	}

	/**
	 * 파일의 정보를 반환 한다.
	 *
	 * @param fileId 파일의 아이디
	 * @return 파일에 대한 정보
	 */
	public FileTable findFileById(@Param("fildId") Long fileId) {
		return postProcessingFileTable(fileTableMapper.findFileById(fileId));
	}

	/**
	 * 유저의 프로필 사진 ID를 반환 한다.
	 * 
	 * @param userId 대상 유저 아이디
	 * @return 파일에 대한 정보
	 */
	public FileTable findUserProfileFileId(@Param("userId") String userId) {
		return postProcessingFileTable(fileTableMapper.findUserProfileFileId(userId));
	}

	/**
	 * 그룹 생성시 그룹의 루트가 될 폴더를 만든다.
	 * DB row 생성 → 물리 폴더 생성(실패 시 롤백)
	 *
	 * @param dto directory 생성할 그룹
	 * @return 생성된 폴더의 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<FileTable> makeGroupDir(GroupTableWithMstUserNameDto dto) {

		ResponseMsg returnMsg = commonMethod.returnResultByResponseMsg(fileTableMapper.makeGroupDir(dto));

		if (!returnMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(returnMsg);
		}

		FileTable fileDto = fileTableMapper.findFileIdByFileOrgNmInDirCase(dto);

		if (null == fileDto || null == fileDto.getFileId()) {
			return ServiceResult.failure(ResponseMsg.BAD_REQUEST);
		}

		// 물리 폴더 생성 (실패 시 예외 -> 트랜잭션 롤백)
		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			createDirectoriesOrThrow(makePathByFileId(fileDto.getFileId())));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findFileById(fileDto.getFileId()));

	}

	/**
	 * 하위 폴더 생성
	 * DB row 생성 → 물리 폴더 생성(실패 시 롤백)
	 *
	 * @param req 폴더 정보가 담긴 DTO
	 * @return 생성된 폴더 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<FileTable>> makeDir(MakeDirDto req) {

		if (fileTableMapper.makeDir(req) != 1) {
			return ServiceResult.failure(ResponseMsg.BAD_REQUEST);
		}

		ResponseMsg fsMsg = commonMethod.returnResultByResponseMsg(
			createDirectoriesOrThrow(Path.of(makePathByFileId(req.getParentId()) + "\\" + req.getDirNm())));

		if (!fsMsg.equals(ResponseMsg.ON_SUCCESS)) {
			return ServiceResult.failure(fsMsg);
		}

		return ServiceResult.success(() -> findFileAndDirectory(req.getParentId(), req.getGroupId()));
	}

	/**
	 * 파일 업로드 비동기 처리를 위한 래퍼 메서드
	 *
	 * @param dto 파일 정보가 담긴 DTO
	 * @return 업로드된 파일 정보
	 */
	@Transactional(rollbackFor = Exception.class)
	@Async("IMGD_VirtualThreadExecutor")
	public CompletableFuture<ServiceResult<FileTable>> makeFileAsync(MakeFileDto dto) {
		return CompletableFuture.supplyAsync(() -> makeFile(dto));
	}

	/**
	 * 파일 생성
	 * DB row 생성 → 물리 폴더 생성(실패 시 롤백)
	 *
	 * @param dto 파일 정보가 담긴 DTO
	 * @return 업로드된 파일 정보
	 *
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<FileTable> makeFile(MakeFileDto dto) {

		String fileNm = UUID.randomUUID().toString();
		String path = findFileNmByDirId(dto.getFolderId());

		dto.setFileNm(fileNm);
		dto.setPath(path);

		if (fileTableMapper.makeFile(dto) != 1) {
			return ServiceResult.failure(ResponseMsg.BAD_REQUEST);
		}

		Path targetNoExt = makePathByFileId(dto.getFolderId());

		Role role = Role.valueOf(userProfilePort.findHighestUserRole(dto.getUserId()).getRoleNm());
		int qValue = role.getPermissionOfWebpWriter()[0];
		int mValue = role.getPermissionOfWebpWriter()[1];
		int zValue = role.getPermissionOfWebpWriter()[2];

		WebpWriter customWriter = WebpWriter.DEFAULT.withQ(qValue).withM(mValue).withZ(zValue);
		Path tmp;

		try {
			tmp = Files.createTempFile("upload-", ".bin");
			dto.getOriginalFile().transferTo(tmp);

		} catch (IOException e) {

			log.error(e.getMessage());
			throw new RuntimeException(e);
		}

		if (null == convertToWebp(targetNoExt, tmp.toFile(), fileNm, customWriter)) {
			return ServiceResult.failure(ResponseMsg.FILE_CREATE_FAILED);
		} else {
			return ServiceResult.success(() -> findFileById(dto.getFileId()));
		}
	}

	@Transactional(rollbackFor = Exception.class)
	@Async("IMGD_VirtualThreadExecutor")
	public CompletableFuture<ServiceResult<UserTableWithRelationshipAndPictureNmDto>> makeUserProfileImgAsync(
		MakeFileDto dto, File originalFile) {
		return CompletableFuture.supplyAsync(() -> makeUserProfileImg(dto, originalFile));
	}

	/**
	 * 유저의 Profile Img 변경 한다.
	 *
	 * @param dto 저장할 파일에 관한 정보가 담긴 dto
	 * @param originalFile 변경할 파일
	 * @return insert 후 결과값
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<UserTableWithRelationshipAndPictureNmDto> makeUserProfileImg(MakeFileDto dto,
		File originalFile) {
		String fileNm = UUID.randomUUID().toString();

		FileTable fileDto = new FileTable();
		fileDto.setFileNm(fileNm);
		fileDto.setFileOrgNm(dto.getFileName());

		// ✅ 파일 테이블에 ROW 생성
		int result = fileTableMapper.makeUserProfileImg(fileDto, dto.getUserId());

		if (result != 1) {
			return ServiceResult.failure(ResponseMsg.BAD_REQUEST);
		}

		// 성공 했다면 user 정보 변경한다.
		long fileId = fileDto.getFileId();

		// ✅ 유저 정보 확인
		UserTableWithRelationshipAndPictureNmDto userProfile = userProfilePort.findUserById(dto.getUserId());

		if (null == userProfile) {
			ServiceResult.failure(ResponseMsg.CAN_NOT_FIND_USER, Map.of("userId", dto.getUserId()));
		}

		// 기존 유저 정보에 사진이 있다면 해당 파일을 삭제한다.
		assert userProfile != null;
		if (null != userProfile.getPictureId()) {
			try {

				boolean resultDeleteFile = deleteFileById(userProfile.getPictureId());

				if (resultDeleteFile) {
					log.info("Deleted file ID: {}", fileId);
				} else {
					log.info("Failed to delete file ID: {}", fileId);
				}

			} catch (Exception e) {
				// 실패해도 신규 사용에는 영향 없음
				log.warn("Best-effort delete of old profile image failed. oldPictureId={}", userProfile.getPictureId(),
					e);
			}
		}

		// ✅ 유저 정보(사진 ID) 변경
		userProfile.setPictureId(fileId);
		int userResult = userProfilePort.updatePictureId(userProfile.getUserId(), fileId);

		if (userResult != 1) {
			return ServiceResult.failure(ResponseMsg.FILE_UPDATE_FAILED);
		}
		Path targetNoExt = makePathByFileId(3L);

		// ✅ 파일 Webp 형태로 변환 및 저장
		if (null == convertToWebp(targetNoExt, originalFile, fileDto.getFileNm(), WebpWriter.DEFAULT)) {
			return ServiceResult.failure(ResponseMsg.BAD_REQUEST);
		} else {
			return ServiceResult.success(() -> userProfilePort.findUserById(dto.getUserId()));
		}
	}

	/**
	 * 파일 삭제
	 *
	 * @param fileId 삭제할 파일 아이디
	 * @return 삭제할 파일이 있는 곳 정보 (parentId)
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<FileTable> deleteFile(Long fileId) {

		FileTable row = findFileById(fileId);

		if (!deleteFileById(fileId)) {
			return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
		}
		return ServiceResult.success(() -> findFileById(row.getParentId()));
	}

	/**
	 * 디렉터리 삭제
	 *
	 * @param fileId 삭제할 디렉터리 아이디
	 * @return 삭제할 디렉터리가 있는 곳 정보 (parentId)
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<FileTable> deleteDir(long fileId) {

		// 빈 객체 선언
		List<FileTable> childFiles;
		Long parentId = findFileById(fileId).getParentId();

		// 해당 id를 부모로 가지고 있는 객체가 없을 때 까지 순환
		do {

			childFiles = findFileByParentId(fileId);

			for (FileTable childFile : childFiles) {
				deleteDir(childFile.getFileId());
			}

		} while (!childFiles.isEmpty());

		// 부모 객체가 없다면 객체 가져오기
		FileTable fileTableSchema = findFileById(fileId);

		// 유형 별 삭제
		if (fileTableSchema.getType().equals("DIR")) {
			if (!deleteDirByFileId(fileTableSchema.getFileId())) {
				return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
			}
		} else {
			if (deleteFileById(fileTableSchema.getFileId())) {
				return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
			}
		}

		return ServiceResult.success(() -> findFileById(parentId));
	}

	/**
	 * 특정 그룹 아이디를 가진 모든 파일을 삭제한다
	 * .
	 * @param userid 시행하는 대상 유저 아이디
	 * @param groupId 대상 그룹 아이디
	 * @return 삭제 후 유저가 가진 그룹 목록 반환
	 */
	@Transactional(rollbackFor = Exception.class)
	public ServiceResult<List<GroupTableWithMstUserNameDto>> deleteFilesByGroupId(String userid, Long groupId) {

		List<FileTable> filesInGroup = fileTableMapper.findFileByGroupId(groupId);

		// 물리 파일 삭제, 파일을 모두 삭제 후 디렉터리도 삭제한다.
		for (FileTable file : filesInGroup) {
			if (!deleteFileById(file.getFileId())) {
				return ServiceResult.failure(ResponseMsg.FILE_DELETE_FAILED);
			}
		}

		return ServiceResult.success(() -> groupPort.findGroupWhatInside(userid));

	}

	/**
	 * 파일을 다운로드 한다.
	 *
	 * @param fileId 다운로드 할 파일의 아이디
	 * @return contentType, Resource, FileOrgNm이 담긴 Map
	 */
	public ServiceResult<Map<String, Object>> downloadFile(Long fileId) {

		FileTable row = findFileById(fileId);
		if (null == row) {
			return ServiceResult.failure(ResponseMsg.NOT_FOUND);
		}

		String filePath = makePathByFileId(fileId) + ".webp";

		Path path = Paths.get(filePath);

		if (Files.notExists(path)) {
			return ServiceResult.failure(ResponseMsg.NOT_FOUND);
		}

		try {

			Resource resource = new UrlResource(path.toUri());

			String contentType = Files.probeContentType(path);
			if (null == contentType) {
				contentType = "application/octet-stream";
			}

			String fileName = row.getFileNm();

			ContentDisposition cd = ContentDisposition.attachment()
				.filename(fileName, StandardCharsets.UTF_8) // ← 한글/이모지 안전
				.build();

			return ServiceResult
				.success(Map.of("contentType", contentType, "resource", resource, "fileInfo", cd.toString()));

		} catch (IOException e) {

			log.error("downloadFile: failed to download file. fileId={}", fileId, e);
			return ServiceResult.failure(ResponseMsg.NOT_FOUND);
		}

	}
	// ───────────────────────────────── helper methods ───────────────────────────────

	/**
	 * 파일 삭제 메서드
	 *
	 * @param fileId 삭제할 파일 아이디
	 * @return 삭제 성공/실패
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteFileById(Long fileId) {
		// 1) 파일 메타 조회
		FileTable row = findFileById(fileId);

		if (null == row) {
			log.warn("deleteFileById: file row not found, fileId={}", fileId);
			return false;
		}

		// 폴더인 경우 디렉터리 삭제로 보낸다.
		if ("DIR".equalsIgnoreCase(row.getType())) {
			return deleteDirByFileId(fileId);
		}

		// 2) 물리 경로 계산
		//    파일은 "부모 폴더 경로" + "fileNm + 확장자"
		Path parentDir = makePathByFileId(row.getParentId());
		// 기본 저장 포맷이 webp:
		Path main = parentDir.resolve(row.getFileNm() + ".webp");

		boolean deletedAny = deleteQuietly(main);
		// 2-1) 같은 baseName 파생 파일들(.jpg, .png, *_thumb.webp 등)도 함께 제거
		deletedAny |= deleteSiblingsByBaseName(parentDir, row.getFileNm());

		if (deletedAny) {
			log.info("deleted physical file ID: {}", fileId);
		} else {
			log.info("failed deleted physical file ID: {}", fileId);
		}

		// 3) DB Row 삭제 (파일 삭제가 일부 실패 해도 DB는 맞춰 준다)
		int db = fileTableMapper.deleteById(fileId);

		return (db == 1);
	}

	/**
	 * 폴더 삭제 메서드
	 *
	 * @param fileId 삭제할 폴더의 id
	 * @return 삭제 결과
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteDirByFileId(Long fileId) {
		FileTable row = findFileById(fileId);
		if (null == row) {
			log.warn("deleteDirByFileId: file row not found, fileId={}", fileId);
			return false;
		}

		// 2) 물리 경로 계산
		//    디렉터리는 "부모 폴더 경로" + "fileNm"
		Path parentDir = makePathByFileId(row.getParentId());
		Path main = parentDir.resolve(row.getFileNm());

		boolean deletedAny = deleteQuietly(main);

		if (deletedAny) {
			log.info("deleted physical Dir ID: {}", fileId);
		} else {
			log.info("failed deleted physical Dir ID: {}", fileId);
		}

		// 3) DB Row 삭제 (파일 삭제가 일부 실패 해도 DB는 맞춰 준다)
		int db = fileTableMapper.deleteById(fileId);

		// 4) 빈 폴더 정리
		cleanupIfEmpty(parentDir);

		return (db == 1);
	}

	/**
	 * 경로 아이디로 파일 이름 반환
	 *
	 * @param dirId 경로 ID
	 * @return 파일 이름
	 */
	public String findFileNmByDirId(Long dirId) {

		return fileTableMapper.findFileNmByDirId(dirId).getFileNm();
	}

	/**
	 * FILE_TABLE 역추적 하여 상대 경로 체인을 반환 한다.
	 *
	 * @param fileId 대상 파일 ID
	 * @return 상대 경로 체인
	 */
	public String findRootPath(Long fileId) {

		StringBuilder sb = new StringBuilder();

		Long cur = fileId;

		while (null != cur) {
			FileTable res = fileTableMapper.findRootPath(cur);
			if (res == null) {
				break;
			}
			if (null != res.getFilePath()) {
				sb.insert(0, "/" + res.getFilePath());
			}
			cur = res.getParentId();
		}
		return sb.toString();

	}

	/**
	 * 파일 아이디를 부모 아이디로 가지고 있는 객체들을 반환한다.
	 * @param fileId 대상 파일 아이디
	 * @return 파일 테이블 객체들
	 */
	public List<FileTable> findFileByParentId(Long fileId) {
		return fileTableMapper.findFileByParentId(fileId);
	}

	/**
	 * 파일의 ID로 경로 찾아 오기
	 *
	 * @param fileId 파일의 ID
	 * @return 파일의 절대 경로
	 */
	public Path makePathByFileId(Long fileId) {

		String relativeChain = findRootPath(fileId);
		String parentName = findFileNmByDirId(fileId);

		return resolveUnderRoot(relativeChain).resolve(sanitizeSegment(parentName));
	}

	/**
	 * 사진 파일을 Webp 형식 파일로 변환 한다.
	 *
	 * @param path 업로드 할 파일의 위치
	 * @param originalFile 업로드 할 파일
	 * @return 변환된 파일
	 */
	public File convertToWebp(Path path, File originalFile, String fileNm, WebpWriter customWriter) {

		// 최종 대상: uuid.webp
		Path target = path.resolve(
			fileNm + ".webp");
		createDirectoriesOrThrow(target.getParent());
		try {

			ImmutableImage.loader()
				.fromFile(originalFile)
				.output(customWriter, target.toFile()); // 여기서 실제 파일 생성

			return target.toFile();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * DB가 돌려준 상대/절대 비슷한 문자열 루트 기준 절대 Path 정규화.
	 *
	 * @param relativeOrAbsolute 대상 문자열
	 * @return 절대 경로
	 */
	private Path resolveUnderRoot(String relativeOrAbsolute) {

		if (null == relativeOrAbsolute || relativeOrAbsolute.isBlank()) {
			return rootPath;
		}

		String cleaned = relativeOrAbsolute.replace("\\", "/").replaceFirst("^/+", "");
		return rootPath.resolve(cleaned).normalize();

	}

	/**
	 * 금칙 문자 치환: Windows 등에서 문제 되는 문자들 '_'로 대체.
	 *
	 * @param name 대상 문자열
	 * @return 치환 문자열
	 */
	private String sanitizeSegment(String name) {
		if (null == name) {
			return "_";
		}
		return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
	}

	/**
	 * 폴더 생성(존재 시 통과). 실패 시 일관된 예외로 래핑.
	 *
	 * @param dir 대상 폴더
	 */
	private int createDirectoriesOrThrow(Path dir) {

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
	 * 환경 변수 읽어 오기
	 * @return 환경 변수
	 */
	private static String resolveRootFromEnv() {
		// 1) JVM 옵션: -D imgd.root.path=...
		String property = System.getProperty("imgd.root.path");
		if (null == property || property.isBlank()) {
			// 2) 환경 변수(우선): IMGD_ROOT_PATH (예: C:/IMGD)
			property = System.getenv("IMGD_ROOT_PATH");
		}
		if (property == null || property.isBlank()) {
			// 3) 레거시 호환
			property = System.getenv("FILE_ROOT");
		}
		return (null == property || property.isBlank()) ? "C:/IMGD" : property;
	}

	/**
	 * 물리 파일을 삭제 한다.
	 *
	 * @param path 대상 경로
	 * @return 결과값
	 */
	private boolean deleteQuietly(Path path) {

		try {
			Files.deleteIfExists(path);
			return true;
		} catch (IOException e) {
			log.warn("Failed to delete file: {}", path, e);
			return false;
		}
	}

	/**
	 * 유사한 이름의 파일을 삭제 한다. (확장자 별)
	 *
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
	 * 폴더가 비었을 경우 폴더를 삭제 한다.
	 *
	 * @param dir 대상 폴더
	 */
	private void cleanupIfEmpty(Path dir) {
		try {
			if (Files.isDirectory(dir) && Files.list(dir).findAny().isEmpty()) {
				Files.delete(dir);
				log.debug("Removed empty dir: {}", dir);
			}
		} catch (IOException e) {
			// 폴더 정리는 선택 사항 이므로 조용히 패스
			log.debug("Skip cleanup for {}: {}", dir, e.toString());
		}
	}

	/**
	 * 파일 목록 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param files 대상 파일 리스트
	 * @return 후처리 후 대상 파일 리스트
	 */
	public List<FileTable> postProcessingFileTables(List<FileTable> files) {

		for (FileTable file : files) {
			postProcessingFileTable(file);
		}

		return files;
	}

	/**
	 * 파일 반환 시 후처리 진행 한다.
	 * DTM(YYYYmmDD) -> YYYY년 MM월 DD일
	 * PictureId 있을 경우 -> PictureUrl Setting
	 *
	 * @param file 대상 파일
	 * @return 후처리 후 대상 파일
	 */
	public FileTable postProcessingFileTable(FileTable file) {

		if (null == file) {
			return null;
		}
		file.setRegDtm(null != file.getRegDtm() ? commonMethod.translateDate(file.getRegDtm()) : null);
		file.setModDtm(null != file.getModDtm() ? commonMethod.translateDate(file.getModDtm()) : null);

		if (null != file.getType() && file.getType().equals("FILE")) {
			file.setFilePath(makePathByFileId(file.getFileId()) + ".webp");
		}
		return file;
	}

}
