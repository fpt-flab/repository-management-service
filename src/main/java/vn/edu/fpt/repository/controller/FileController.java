package vn.edu.fpt.repository.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.repository.dto.common.GeneralResponse;
import vn.edu.fpt.repository.dto.common.PageableResponse;
import vn.edu.fpt.repository.dto.request.file.CreateFileRequest;
import vn.edu.fpt.repository.dto.request.file.UpdateFileRequest;
import vn.edu.fpt.repository.dto.response.file.CreateFileResponse;
import vn.edu.fpt.repository.dto.response.file.GetFileDetailResponse;
import vn.edu.fpt.repository.dto.response.file.GetFileResponse;

/**
 * @author : Hoang Lam
 * @product : Charity Management System
 * @project : Charity System
 * @created : 29/11/2022 - 15:28
 * @contact : 0834481768 - hoang.harley.work@gmail.com
 **/
@RequestMapping("${app.application-context}/public/api/v1/files")
public interface FileController {
    @PutMapping("/{file-id}")
    ResponseEntity<GeneralResponse<Object>> updateFile(@PathVariable(name = "file-id") String fileId, @RequestBody UpdateFileRequest request);

    @DeleteMapping("/{file-id}")
    ResponseEntity<GeneralResponse<Object>> deleteFile(@PathVariable(name = "file-id") String fileId);

    @GetMapping
    ResponseEntity<GeneralResponse<PageableResponse<GetFileResponse>>> getFile();

    @GetMapping("/{file-id}")
    ResponseEntity<GeneralResponse<GetFileDetailResponse>> getFileDetail(@PathVariable(name = "file-id") String fileId);
}
