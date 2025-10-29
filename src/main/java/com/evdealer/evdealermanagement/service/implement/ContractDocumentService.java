package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.entity.transactions.ContractDocument;
import com.evdealer.evdealermanagement.repository.ContractDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractDocumentService {

    private final ContractDocumentRepository contractRepository;

    public Page<ContractDocument> getAllContracts(Pageable pageable) {
        Page<ContractDocument> page = contractRepository.findAll(pageable);
        List<ContractDocument> sortedList = page.getContent()
                .stream()
                .sorted(Comparator.comparing(ContractDocument::getSignedAt).reversed())
                .toList();
        return new PageImpl<>(sortedList, pageable, page.getTotalElements());
    }
}
