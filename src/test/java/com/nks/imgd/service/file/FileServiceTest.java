package com.nks.imgd.service.file;

import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.mapper.file.FileTableMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import javax.imageio.ImageIO;

/**
 * Unit test for {@link FileService}.
 * <p>
 * This test verifies that a group is correctly inserted through
 * {@link FileService#makeGroupDir(GroupTableDTO)} 
 * {@link FileService#makeDir(String, Long, Long, String)}
 * {@link FileService#makeFile(Long, String, Long, String, File)}
 * using a mocked {@link FileTableMapper}.
 */
class FileServiceTest {

	@TempDir
	Path tempDir; // 임시 Directory

	FileTableMapper mapper;
	FileService service;

	@BeforeEach
	void setUp() {
		mapper = mock(FileTableMapper.class);
		service = new FileService(mapper, tempDir.toString());

	}

	@Test
	void makeGroupDirCreatesPhysicalDirectoryWhenMapperSucceeds() {

		// ✅ Given
		GroupTableDTO dto = new GroupTableDTO();
		dto.setGroupId(1L);
		dto.setGroupNm("테스트 그룹");
		dto.setGroupMstUserId("ksna");

		// 신규 디렉터리의 FILE_ID 반환
		FileTableDTO idRow = new FileTableDTO();
		idRow.setFileId(999L);

		// 루트 경로 조회(서비스의 selectRootPath는 "/"+FILE_PATH 누적)
		FileTableDTO pathRow = new FileTableDTO();
		// OS 경로 구분자 문제 최소화를 위해 슬래시 통일
		pathRow.setFilePath(tempDir.toString().replace("\\", "/"));
		pathRow.setParentId(null);

		// ✅ When
		when(mapper.makeGroupDir(any())).thenReturn(1);
		when(mapper.selectFileIdByFileOrgNmInDirCase(eq(dto))).thenReturn(idRow);
		when(mapper.selectRootPath(999L)).thenReturn(pathRow);
		int result = service.makeGroupDir(dto);

		// ✅ Then
		assertEquals(1, result);

		// 서비스가 만든 실제 경로를 동일 로직으로 구성해 예상값 비교
		File expected = new File("/" + pathRow.getFilePath() + "/" + dto.getGroupId() + "_" + dto.getGroupNm());
		assertTrue(expected.exists() && expected.isDirectory(), "그룹 디렉터리가 실제로 생성되어야 합니다.");

		// 호출 순서 느슨 검증
		InOrder io = inOrder(mapper);
		io.verify(mapper).makeGroupDir(dto);
		io.verify(mapper).selectFileIdByFileOrgNmInDirCase(dto);
		io.verify(mapper).selectRootPath(999L);
	}

	@Test
	void makeGroupDirReturnsMinus1WhenMapperInsertFails() {

		// ✅ Given
		GroupTableDTO dto = new GroupTableDTO();

		// ✅ When
		when(mapper.makeGroupDir(any())).thenReturn(0);
		int result = service.makeGroupDir(dto);

		// ✅ Then
		assertEquals(-1, result);
		verify(mapper, never()).selectFileIdByFileOrgNmInDirCase(any());
		verify(mapper, never()).selectRootPath(anyLong());
	}

	@Test
	void makeDirCreatesChildDirectoryUnderParent() {

		// ✅ Given
		String userId = "nks";
		Long parentId = 10L;
		Long groupId = 1L;
		String parentName = "1_테스트 그룹";
		String childName = "추억";

		// 부모 경로: tempDir
		FileTableDTO root = new FileTableDTO();
		root.setFilePath(tempDir.toString().replace("\\", "/"));
		root.setParentId(null);

		// 부모 이름 조회(서비스 코드상 2회 호출)
		FileTableDTO nameRow = new FileTableDTO();
		nameRow.setFileNm(parentName);

		// ✅ When
		when(mapper.selectRootPath(parentId)).thenReturn(root);
		when(mapper.selectFileNmByDirId(parentId)).thenReturn(nameRow);
		// DB insert 성공
		when(mapper.makeDir(eq(userId), eq(childName), eq(parentName), eq(parentId), eq(groupId))).thenReturn(1);

		int result = service.makeDir(userId, parentId, groupId, childName);

		// ✅ Then
		assertEquals(1, result);
		File expected = new File("/" + root.getFilePath() + "/" + parentName + "/" + childName);
		assertTrue(expected.exists() && expected.isDirectory(), "하위 디렉터리가 실제로 생성되어야 합니다.");
	}

	@Test
	void makeFileWhenSuccess()
	{

		// ✅ Given
		Long folderId = 5L;
		String userId = "ksna";
		Long groupId = 1L;
		String fileOrgNm = "KakaoTalk_20250515_233719969.jpg";

		// 임시 원본 파일 생성
		File originalFile = new File(tempDir.toFile(), "input.png");

		try {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			ImageIO.write(img, "png", originalFile);
		} catch (IOException e) {
			fail("원본 파일 생성 실패");
		}

		String parentName = "1_테스트 그룹";
		FileTableDTO nameRow = new FileTableDTO();
		nameRow.setFileNm(parentName);

		// selectRootPath()는 상대 세그먼트만 제공
		FileTableDTO rootRow = new FileTableDTO();
		rootRow.setFilePath("GROUP_IMG");
		rootRow.setParentId(null);

		// ✅ Stubbing
		when(mapper.selectFileNmByDirId(folderId)).thenReturn(nameRow);
		when(mapper.selectRootPath(folderId)).thenReturn(rootRow);
		when(mapper.makeFile(anyString(), eq(fileOrgNm), eq(parentName), eq(folderId), eq(groupId), eq(userId)))
			.thenReturn(1);

		// ✅ When
		int result = service.makeFile(folderId, userId, groupId, fileOrgNm, originalFile);

		// ✅ Then
		assertEquals(1, result);


		// 실제 생성된 webp 파일 탐색 후 부모 디렉터리 검증
		try {
			Path webp = Files.walk(tempDir)
				.filter(p -> p.getFileName().toString().endsWith(".webp"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("webp 파일이 생성되지 않았습니다."));

			Path parentDir = webp.getParent();
			assertNotNull(parentDir);

			// 기대: <tempDir>/GROUP_IMG/<parentName>
			Path expectedDir = tempDir.resolve("GROUP_IMG").resolve(parentName);
			assertEquals(expectedDir.normalize().toString(), parentDir.normalize().toString(),
				"webp 부모 디렉터리가 기대 경로와 일치해야 합니다.");

			assertTrue(Files.exists(webp) && Files.size(webp) > 0, "webp 파일이 실제로 생성되어야 합니다.");

		} catch (IOException e) {
			fail("파일 탐색 중 오류: " + e.getMessage());
		}

		verify(mapper).makeFile(anyString(), eq(fileOrgNm), eq(parentName), eq(folderId), eq(groupId), eq(userId));

	}

	@Test
	void makeFileWhenFail()
	{
		// ✅ Given
		Long folderId = 5L;
		String userId = "ksna";
		Long groupId = 1L;
		String fileOrgNm = "KakaoTalk_20250515_233719969.jpg";

		File originalFile = new File(tempDir.toFile(), "input.png");
		try {
			BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			ImageIO.write(img, "png", originalFile);
		} catch (IOException e) {
			fail("원본 파일 생성 실패: " + e.getMessage());
		}

		String parentName = "1_테스트 그룹";
		FileTableDTO nameRow = new FileTableDTO();
		nameRow.setFileNm(parentName);

		FileTableDTO rootRow = new FileTableDTO();
		rootRow.setFilePath("GROUP_IMG");
		rootRow.setParentId(null);

		// ✅ Stubbing
		when(mapper.selectFileNmByDirId(folderId)).thenReturn(nameRow);
		when(mapper.selectRootPath(folderId)).thenReturn(rootRow);
		when(mapper.makeFile(anyString(), eq(fileOrgNm), eq(parentName), eq(folderId), eq(groupId), eq(userId)))
			.thenReturn(0); // insert 실패

		// ✅ When
		int result = service.makeFile(folderId, userId, groupId, fileOrgNm, originalFile);

		// ✅ Then
		assertEquals(-1, result);

		try {
			boolean existsWebp = Files.walk(tempDir)
				.anyMatch(p -> p.getFileName().toString().endsWith(".webp"));
			assertFalse(existsWebp, "insert 실패 시 webp가 생성되면 안 됩니다.");
		} catch (IOException e) {
			fail("파일 탐색 중 오류: " + e.getMessage());
		}

		verify(mapper).makeFile(anyString(), eq(fileOrgNm), eq(parentName), eq(folderId), eq(groupId), eq(userId));

	}


}
