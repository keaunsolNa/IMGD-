package com.nks.imgd.service.file;

import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.mapper.file.FileTableMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link FileService}.
 * <p>
 * This test verifies that a group is correctly inserted through
 * {@link FileService#makeGroupDir(GroupTableDTO)} 
 * {@link FileService#makeDir(String, Long, Long, String)}
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
		service = new FileService(mapper);
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

}
