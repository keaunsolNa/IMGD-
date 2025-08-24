package com.nks.imgd.controller.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nks.imgd.dto.data.MakeFileDTO;
import com.nks.imgd.dto.group.GroupTableDTO;
import com.nks.imgd.dto.data.MakeDirDTO;
import com.nks.imgd.service.file.FileService;

@RestController
@RequestMapping("/file")
public class FileController {

	private final FileService fileService;
	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@PostMapping("/makeGroupDir")
	public ResponseEntity<String> makeGroupDir(@RequestBody GroupTableDTO dto, @AuthenticationPrincipal Jwt jwt) {

		dto.setGroupMstUserId(jwt.getSubject());

		int inserted = fileService.makeGroupDir(dto);
		if (inserted > 0) {
			return ResponseEntity.ok("Complete make group root directory");
		}
		else
		{
			return ResponseEntity.internalServerError().body("Failed make group root directory");
		}
	}

	@PostMapping("/makeDir")
	public ResponseEntity<String> makeDir(@RequestBody MakeDirDTO req) {

		int inserted = fileService.makeDir(req);
		if (inserted > 0) {
			return ResponseEntity.ok("Complete make group root directory");
		}
		else
		{
			return ResponseEntity.internalServerError().body("Failed make group root directory");
		}
	}

	@PostMapping(value = "/makeFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> makeFile(@ModelAttribute MakeFileDTO req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");
		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {
			int inserted = fileService.makeFile(
				req.getFolderId(),
				req.getUserId(),
				req.getGroupId(),
				mf.getOriginalFilename(),     // DB의 원본명 컬럼에는 실제 업로드 파일명 사용
				tmp.toFile()
			);
			return inserted > 0
				? ResponseEntity.ok("Complete make file")
				: ResponseEntity.internalServerError().body("Failed make file");
		} finally {
			Files.deleteIfExists(tmp); // 임시파일 정리
		}
	}

	@PostMapping(value = "/makeUserProfileImg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> makeUserProfileImg(@ModelAttribute MakeFileDTO req) throws IOException {

		Path tmp = Files.createTempFile("upload-", ".bin");
		MultipartFile mf = req.getOriginalFile();
		mf.transferTo(tmp);

		try {
			int inserted = fileService.makeUserProfileImg(req, tmp.toFile());
			return inserted > 0
				? ResponseEntity.ok("Complete make file")
				: ResponseEntity.internalServerError().body("Failed make file");
		} finally {
			Files.deleteIfExists(tmp); // 임시파일 정리
		}
	}


}
