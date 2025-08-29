package com.nks.imgd.controller.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nks.imgd.dto.data.MakeFileDTO;
import com.nks.imgd.dto.file.FileTableDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.dto.data.MakeDirDTO;
import com.nks.imgd.dto.user.UserTableDTO;
import com.nks.imgd.service.file.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	private final FileService fileService;

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
	public ResponseEntity<List<FileTableDTO>> findFileAndDirectory(@RequestParam Long parentId, @RequestParam Long groupId) {
		return ResponseEntity.ok(fileService.findFileAndDirectory(parentId, groupId));
	}

	/**
	 * 파일을 반환 한다.
	 * @param fileId 대상 파일 아이디
	 * @return 대상 파일 정보
	 */
	@GetMapping("findFileById")
	public ResponseEntity<FileTableDTO> FileTableDTO(@RequestParam Long fileId) {
		return ResponseEntity.ok(fileService.findFileById(fileId));
	}

	/**
	 * 그룹의 Root Folder 생성 한다.
	 * @param dto 유저가 선택한 그룹에 대한 정보
	 * @param jwt 유저의 토큰
	 * @return 해당 폴더 정보 목록
	 */
	@PostMapping("/makeGroupDir")
	public ResponseEntity<FileTableDTO> makeGroupDir(@RequestBody GroupTableDTO dto, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());
		return fileService.makeGroupDir(dto);

	}

	/**
	 * 폴더를 생성 한다.
	 * @param req 폴더 생성에 필요한 정보
	 * @return 생성된 폴더가 위치한 곳의 파일 / 폴더 목록
	 */
	@PostMapping("/makeDir")
	public ResponseEntity<List<FileTableDTO>> makeDir(@RequestBody MakeDirDTO req) {
		return fileService.makeDir(req);
	}

	/**
	 * 파일을 생성 한다
	 * @param req 파일 생성에 필요한 정보
	 * @return 생성한 파일 정보
	 * @throws IOException 파일 생성 실패 시 IOException 반환
	 */
	@PostMapping(value = "/makeFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FileTableDTO> makeFile(@ModelAttribute MakeFileDTO req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");
		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {
			return fileService.makeFile(
				req.getFolderId(),
				req.getUserId(),
				req.getGroupId(),
				mf.getOriginalFilename(),     // DB의 원본명 컬럼에는 실제 업로드 파일명 사용
				tmp.toFile()
			);
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
	public ResponseEntity<UserTableDTO> makeUserProfileImg(@ModelAttribute MakeFileDTO req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");

		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {
			return fileService.makeUserProfileImg(req, tmp.toFile());
		} finally {
			Files.deleteIfExists(tmp); // 임시 파일 정리
		}
	}


}
