package com.nks.imgd.service.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.mapper.file.FileTableMapper;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

import org.springframework.beans.factory.annotation.Autowired;
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
	private final Path rootPath;

	/** 운영 기본: 시스템 프로퍼티/환경변수에서 루트 경로 로드 */
	@Autowired
	public FileService(FileTableMapper fileTableMapper) {
		this(fileTableMapper, resolveRootFromEnv());
	}

	/** 테스트/특수 상황용: 직접 루트 경로 주입 */
	public FileService(FileTableMapper fileTableMapper, String root) {
		this.fileTableMapper = fileTableMapper;
		this.rootPath = Paths.get(root).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.rootPath); // 존재 보장
		} catch (IOException e) {
			throw new IllegalStateException("Cannot create root directory: " + this.rootPath, e);
		}
		log.info("IMGD root path = {}", this.rootPath);
	}

    /**
     * @param dto directory 생성할 그룹
	 * @return int 결과 값
	 * 그룹 생성시 그룹의 루트가 될 폴더를 만든다.
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
     */
	@Transactional(rollbackFor = Exception.class)
    public int makeGroupDir(GroupTableDTO dto)
    {

		System.out.println(dto);
        int result = fileTableMapper.makeGroupDir(dto);

		if (result != 1) return -1;

		FileTableDTO fileDTO = fileTableMapper.selectFileIdByFileOrgNmInDirCase(dto);

		System.out.println(fileDTO);
		if (fileDTO == null || fileDTO.getFileId() == null) {
			throw new IllegalStateException("Inserted group row not found: " + dto);
		}

		String relativeChain = selectRootPath(fileDTO.getFileId());   // ex) "/GROUP_IMG"
		Path base = resolveUnderRoot(relativeChain);
		String groupFolder = sanitizeSegment(dto.getGroupId() + "_" + dto.getGroupNm());
		Path target = base.resolve(groupFolder);

		System.out.println(target);
		createDirectoriesOrThrow(target);
		log.debug("Created group dir: {}", target);
		return 1;

	}

	/**
	 * @param userId 폴더를 생성하는 그룹의 MST_USER_ID
	 * @param parentId 폴더가 생성될 부모 폴더 ID
	 * @param groupId 폴더 생성될 그룹 ID
	 * @param dirNm 생성할 폴더 이름
	 * @return int 결과 값
	 * 하위 디렉터리 생성
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
	 */
	@Transactional(rollbackFor = Exception.class)
    public int makeDir(String userId, Long parentId, Long groupId, String dirNm)
    {

		String parentName = selectFileNmByDirId(parentId);
		String relativeChain = selectRootPath(parentId);

        int result = fileTableMapper.makeDir(userId, dirNm, selectFileNmByDirId(parentId), parentId, groupId);
		if (result != 1) return -1;

		Path base = resolveUnderRoot(relativeChain).resolve(sanitizeSegment(parentName));
		Path target = base.resolve(sanitizeSegment(dirNm));

		createDirectoriesOrThrow(target);
		log.debug("Created child dir: {}", target);
		return 1;

    }

	/**
	 * @param folderId 폴더가 생성될 부모 폴더 ID
	 * @param userId 폴더를 생성하는 그룹의 MST_USER_ID
	 * @param groupId 폴더가 생성될 그룹 ID
	 * @param fileOrgNm 생성할 파일 원본 이름
	 * @param originalFile 생성할 파일
	 * @return int 결과 값
	 * 파일 생성
	 * TODO : 그룹 계정의 과금 권한에 따른 파일 용량/압축률/최대크기등 설정
	 * DB row 생성 → 물리 디렉터리 생성(실패 시 롤백)
	 */
	@Transactional(rollbackFor = Exception.class)
	public int makeFile(Long folderId, String userId, Long groupId, String fileOrgNm, File originalFile)
	{

		String parentName = selectFileNmByDirId(folderId);
		String relativeChain = selectRootPath(folderId);
		String fileNm = UUID.randomUUID().toString();
		String path = selectFileNmByDirId(folderId);

		int result = fileTableMapper.makeFile(fileNm, fileOrgNm, path, folderId, groupId, userId);
		if (result != 1) return -1;

		Path base        = resolveUnderRoot(relativeChain).resolve(sanitizeSegment(parentName));
		Path targetNoExt = base.resolve(sanitizeSegment(fileNm)); // ✅ 실제 저장 파일명 = storedName

		File webp = convertToWebp(targetNoExt, originalFile);

		log.debug("Created file: {}", webp.getAbsolutePath());
		return 1;
	}

	public File convertToWebp(Path path, File originalFile)
	{

		// 최종 대상: uuid.webp
		Path target = path.resolveSibling(
			path.getFileName().toString() + ".webp"
		);

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

	public File convertToWebpWithLossless(String path, File originalFile)
	{
		try
		{
			return ImmutableImage.loader() // 라이브러리 객체 생성
				.fromFile(originalFile) // .jpg or .png File 가져옴
				.output(WebpWriter.DEFAULT.withLossless(), new File(path + ".webp")); // 무손실 압축 설정, fileName.webp로 파일 생성
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	// ───────────────────────────────── helper methods ───────────────────────────────

	/** FILE_TABLE를 역추적하여 상대 경로 체인을 돌려준다. */
	public String selectRootPath(Long fileId) {

		StringBuilder sb = new StringBuilder();

		Long cur = fileId;

		while (cur != null) {
			FileTableDTO r = fileTableMapper.selectRootPath(cur);
			if (r == null) break;
			if (r.getFilePath() != null) {
				sb.insert(0, "/" + r.getFilePath());
			}
			cur = r.getParentId();
		}
		return sb.toString();

	}

	public String selectFileNmByDirId(Long dirId) {

		return fileTableMapper.selectFileNmByDirId(dirId).getFileNm();
	}

	/** DB가 돌려준 상대/절대 비슷한 문자열을 루트 기준 절대 Path로 정규화. */
	private Path resolveUnderRoot(String relativeOrAbsolute) {

		if (relativeOrAbsolute == null || relativeOrAbsolute.isBlank()) {
			return rootPath;
		}

		String cleaned = relativeOrAbsolute.replace("\\", "/").replaceFirst("^/+", "");
		return rootPath.resolve(cleaned).normalize();

	}

	/** 금칙문자 치환: Windows 등에서 문제되는 문자들을 '_'로 대체. */
	private String sanitizeSegment(String name) {
		if (name == null) return "_";
		return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
	}

	/** 디렉터리 생성(존재 시 통과). 실패 시 일관된 예외로 래핑. */
	private void createDirectoriesOrThrow(Path dir) {
		try {
			Files.createDirectories(dir);
		} catch (FileAlreadyExistsException e) {
			// 같은 이름의 "파일"이 이미 있는 경우
			throw new IllegalStateException("Path exists but is not a directory: " + dir, e);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create directory: " + dir, e);
		}
	}

	// 환경변수 읽어오기
	private static String resolveRootFromEnv() {
		// 1) JVM 옵션: -Dimgd.root.path=...
		String v = System.getProperty("imgd.root.path");
		if (v == null || v.isBlank()) {
			// 2) 환경변수(우선): IMGD_ROOT_PATH (예: C:/IMGD)
			v = System.getenv("IMGD_ROOT_PATH");
		}
		if (v == null || v.isBlank()) {
			// 3) 레거시 호환
			v = System.getenv("FILE_ROOT");
		}
		return (v == null || v.isBlank()) ? "C:/IMGD" : v;
	}
}
