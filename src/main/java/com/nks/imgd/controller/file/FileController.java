package com.nks.imgd.controller.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nks.imgd.component.util.commonmethod.CommonMethod;
import com.nks.imgd.component.util.maker.ApiResponse;
import com.nks.imgd.component.util.maker.ServiceResult;
import com.nks.imgd.dto.data.GroupTableWithMstUserNameDto;
import com.nks.imgd.dto.data.MakeDirDto;
import com.nks.imgd.dto.data.MakeFileDto;
import com.nks.imgd.dto.data.UserTableWithRelationshipAndPictureNmDto;
import com.nks.imgd.dto.schema.FileTable;
import com.nks.imgd.service.file.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	private final FileService fileService;
	private static final CommonMethod commonMethod = new CommonMethod();

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	/**
	 * 해당 위치에 존재 하는 파일 / 폴더 목록을 반환 한다.
	 * @param parentId 현재 유저가 위치한 폴더 아이디
	 * @param groupId 현재 유저가 사용 하는 그룹 아이디
	 * @return 해당 위치에 존재 하는 파일 / 폴더 목록
	 */
	@GetMapping("/findFileAndDirectory")
	public ResponseEntity<List<FileTable>> findFileAndDirectory(@RequestParam Long parentId,
		@RequestParam Long groupId) {
		return ResponseEntity.ok(fileService.findFileAndDirectory(parentId, groupId));
	}

	/**
	 * 파일을 반환 한다.
	 * @param fileId 대상 파일 아이디
	 * @return 대상 파일 정보
	 */
	@GetMapping("/findFileById")
	public ResponseEntity<FileTable> findFileById(@RequestParam Long fileId) {
		return ResponseEntity.ok(fileService.findFileById(fileId));
	}

	/**
	 * 파일을 다운로드 한다.
	 * 
	 * @param fileId 다운로드 받을 파일의 ID
	 * @return 파일
	 */
	@GetMapping("/downloadFile")
	public ResponseEntity<Resource> downloadFile(@RequestParam Long fileId) {

		Map<String, Object> map = fileService.downloadFile(fileId).details();

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType((String)map.get("contentType")))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + map.get("fileInfo") + "\"")
			.body((Resource)map.get("resource"));
	}

	/**
	 * 그룹의 Root Folder 생성 한다.
	 * @param dto 유저가 선택한 그룹에 대한 정보
	 * @param jwt 유저의 토큰
	 * @return 해당 폴더 정보 목록
	 */
	@PostMapping("/makeGroupDir")
	public ResponseEntity<ApiResponse<FileTable>> makeGroupDir(@RequestBody GroupTableWithMstUserNameDto dto,
		@AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());
		return commonMethod.responseTransaction(fileService.makeGroupDir(dto));
	}

	/**
	 * 폴더를 생성 한다.
	 * @param req 폴더 생성에 필요한 정보
	 * @return 생성된 폴더가 위치한 곳의 파일 / 폴더 목록
	 */
	@PostMapping("/makeDir")
	public ResponseEntity<ApiResponse<List<FileTable>>> makeDir(@RequestBody MakeDirDto req) {
		return commonMethod.responseTransaction(fileService.makeDir(req));
	}

	/**
	 * 파일을 생성 한다
	 * @param req 파일 생성에 필요한 정보
	 * @return 생성한 파일 정보
	 * @throws IOException 파일 생성 실패 시 IOException 반환
	 */
	@PostMapping(value = "/makeFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<FileTable>> makeFile(@ModelAttribute MakeFileDto req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");
		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {

			MakeFileDto file = new MakeFileDto();
			file.setFolderId(req.getFolderId());
			file.setUserId(req.getUserId());
			file.setGroupId(req.getGroupId());
			file.setFileOrgNm(mf.getOriginalFilename());
			file.setOriginalFile(mf);

			CompletableFuture<ServiceResult<FileTable>> future = fileService.makeFileAsync(file);

			ServiceResult<FileTable> result = future.join();
			return commonMethod.responseTransaction(result);

		} finally {
			Files.deleteIfExists(tmp); // 임시파일 정리
		}
	}

	/**
	 * 유저의 프로필 사진을 변경 / 업로드 한다.
	 * @param req 프로필 사진에 대한 정보
	 * @return 유저의 정보
	 * @throws IOException 파일 업로드 실패 시 IOException 반환
	 */
	@PostMapping(value = "/makeUserProfileImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<UserTableWithRelationshipAndPictureNmDto>> makeUserProfileImg(
		@ModelAttribute MakeFileDto req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");

		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {

			CompletableFuture<ServiceResult<UserTableWithRelationshipAndPictureNmDto>> future = fileService
				.makeUserProfileImgAsync(req, tmp.toFile());

			ServiceResult<UserTableWithRelationshipAndPictureNmDto> result = future.join();
			return commonMethod.responseTransaction(result);

		} finally {
			Files.deleteIfExists(tmp); // 임시 파일 정리
		}
	}

	/**
	 * 파일을 삭제한다.
	 * @param fileId 삭제할 파일 아이디
	 * @return 삭제할 파일이 위치한 디렉터리 정보
	 */
	@DeleteMapping(value = "/deleteFile")
	public ResponseEntity<ApiResponse<FileTable>> deleteFile(@RequestParam Long fileId) {
		return commonMethod.responseTransaction(fileService.deleteFile(fileId));
	}

	/**
	 * 디렉터리를 삭제한다.
	 * 이 때 하위 파일/폴더가 있다면 같이 삭제한다.
	 * @param fileId 삭제할 디렉터리 아이디
	 * @return 삭제된 디렉터리의 부모 객체 정보
	 */
	@DeleteMapping(value = "/deleteDir")
	public ResponseEntity<ApiResponse<FileTable>> deleteDir(@RequestParam Long fileId) {
		return commonMethod.responseTransaction(fileService.deleteDir(fileId));
	}
}
