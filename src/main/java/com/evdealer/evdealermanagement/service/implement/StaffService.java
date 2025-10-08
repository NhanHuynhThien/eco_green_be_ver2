package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StaffService {

    @Autowired
    private ProductRepository productRepository;

    
}
