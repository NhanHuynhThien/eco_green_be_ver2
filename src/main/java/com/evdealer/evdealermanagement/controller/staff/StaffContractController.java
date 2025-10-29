package com.evdealer.evdealermanagement.controller.staff;

import com.evdealer.evdealermanagement.dto.common.PageResponse;
import com.evdealer.evdealermanagement.entity.transactions.ContractDocument;
import com.evdealer.evdealermanagement.service.implement.ContractDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/contracts")
@Slf4j
@RequiredArgsConstructor
public class StaffContractController {

    private final ContractDocumentService contractDocumentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<PageResponse<ContractDocument>> getAllContracts(
            @PageableDefault(page = 0, size = 12, sort = "signedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            log.info("Request → Get all contract documents");
            Page<ContractDocument> page = contractDocumentService.getAllContracts(pageable);

            if (page.isEmpty()) {
                log.info("No contracts found");
                return ResponseEntity.noContent().build();
            }

            PageResponse<ContractDocument> response = PageResponse.<ContractDocument>builder()
                    .items(page.getContent())
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();

            log.info("Found {} contracts", page.getTotalElements());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error while fetching contracts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
