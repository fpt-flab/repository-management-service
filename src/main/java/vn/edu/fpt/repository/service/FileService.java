package vn.edu.fpt.repository.service;

import vn.edu.fpt.repository.dto.common.PageableResponse;
import vn.edu.fpt.repository.dto.request.file.CreateFileRequest;
import vn.edu.fpt.repository.dto.request.file.GetFileRequest;
import vn.edu.fpt.repository.dto.request.file.UpdateFileRequest;
import vn.edu.fpt.repository.dto.response.file.CreateFileResponse;
import vn.edu.fpt.repository.dto.response.file.GetFileDetailResponse;
import vn.edu.fpt.repository.dto.response.file.GetFileResponse;

/**
 * @author : Hoang Lam
 * @product : Charity Management System
 * @project : Charity System
 * @created : 29/11/2022 - 16:26
 * @contact : 0834481768 - hoang.harley.work@gmail.com
 **/
public interface FileService {

    CreateFileResponse addFileToFolder(String folderId, CreateFileRequest request);

    void updateFile(String fileId, UpdateFileRequest request);

    void deleteFile(String fileId);

    PageableResponse<GetFileResponse> getFile(GetFileRequest request);

    GetFileDetailResponse getFileDetail(String fileId);
}
