package com.example.shopapp.mapper;

import com.example.shopapp.dto.response.ShopApprovalResponseDTO;
import com.example.shopapp.model.ShopApproval;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShopApprovalMapper {

    public ShopApprovalResponseDTO toDTO(ShopApproval shopApproval) {
        if (shopApproval == null) {
            return null;
        }

        ShopApprovalResponseDTO dto = new ShopApprovalResponseDTO();
        dto.setId(shopApproval.getId());
        dto.setShopId(shopApproval.getShop().getId());
        dto.setApprovalStatus(shopApproval.getApprovalStatus());
        dto.setApproved(shopApproval.getApproved());
        dto.setReason(shopApproval.getReason());
//        dto.setCreatedAt(shopApproval.getCreatedAt());
//        dto.setUpdatedAt(shopApproval.getUpdatedAt());
        return dto;
    }

    public List<ShopApprovalResponseDTO> toDTOList(List<ShopApproval> approvals) {
        return approvals.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
