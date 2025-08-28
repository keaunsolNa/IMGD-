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

	@GetMapping("/findFileAndDirectory")
	public ResponseEntity<List<FileTableDTO>> findFileAndDirectory(@RequestParam Long parentId, @RequestParam Long groupId) {
		return ResponseEntity.ok(fileService.findFileAndDirectory(parentId, groupId));
	}

	@PostMapping("/makeGroupDir")
	public ResponseEntity<FileTableDTO> makeGroupDir(@RequestBody GroupTableDTO dto, @AuthenticationPrincipal Jwt jwt) {
		dto.setGroupMstUserId(jwt.getSubject());
		return fileService.makeGroupDir(dto);

	}

	@PostMapping("/makeDir")
	public ResponseEntity<FileTableDTO> makeDir(@RequestBody MakeDirDTO req) {
		return fileService.makeDir(req);
	}

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

	@PostMapping(value = "/makeUserProfileImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserTableDTO> makeUserProfileImg(@ModelAttribute MakeFileDTO req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");

		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {
			return fileService.makeUserProfileImg(req, tmp.toFile());
		} finally {
			Files.deleteIfExists(tmp); // 임시파일 정리
		}
	}


}
