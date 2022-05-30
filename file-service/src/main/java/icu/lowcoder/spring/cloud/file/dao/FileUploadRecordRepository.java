package icu.lowcoder.spring.cloud.file.dao;

import icu.lowcoder.spring.cloud.file.entity.FileUploadRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface FileUploadRecordRepository extends PagingAndSortingRepository<FileUploadRecord, UUID> {

}
