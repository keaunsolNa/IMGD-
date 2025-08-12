package com.nks.imgd.controller.file;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<String> makeGroupDir(@RequestBody GroupTableDTO dto) {

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

		int inserted = fileService.makeDir(req.getUserId(), req.getParentId(), req.getDirNm());
		if (inserted > 0) {
			return ResponseEntity.ok("Complete make group root directory");
		}
		else
		{
			return ResponseEntity.internalServerError().body("Failed make group root directory");
		}
	}
}
