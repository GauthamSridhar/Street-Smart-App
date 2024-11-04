package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.ShopApproval;
import com.example.shopapp.model.User;
import com.example.shopapp.repository.ShopApprovalRepository;
import com.example.shopapp.repository.UserRepository; // Import UserRepository to fetch existing User
import com.example.shopapp.service.ShopApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopApprovalServiceImpl implements ShopApprovalService {
    private final ShopApprovalRepository shopApprovalRepository;
    private final UserRepository userRepository; // Inject UserRepository

    @Override
    public ShopApproval approveShop(UUID adminId, UUID shopId) {
        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval for shop with ID " + shopId + " not found"));

        approval.setApprovalStatus("APPROVED"); // Assuming status is a String, but consider using an Enum
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin with ID " + adminId + " not found"));
        approval.setAdmin(admin);

        return shopApprovalRepository.save(approval);
    }

    @Override
    public ShopApproval rejectShop(UUID adminId, UUID shopId, String reason) {
        ShopApproval approval = shopApprovalRepository.findByShopId(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval for shop with ID " + shopId + " not found"));

        approval.setApprovalStatus("REJECTED"); // Correctly set the approval status
        approval.setReason(reason);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin with ID " + adminId + " not found"));
        approval.setAdmin(admin);

        return shopApprovalRepository.save(approval);
    }

    @Override
    public List<ShopApproval> getPendingApprovals() {
        return shopApprovalRepository.findByApprovalStatus("PENDING");
    }

    @Override
    public ShopApproval getApprovalById(UUID approvalId) {
        return shopApprovalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop approval with ID " + approvalId + " not found"));
    }
}
