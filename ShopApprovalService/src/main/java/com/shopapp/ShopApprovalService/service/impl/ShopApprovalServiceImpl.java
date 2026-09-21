package com.shopapp.ShopApprovalService.service.impl;

import com.shopapp.ShopApprovalService.dto.shopapproval.response.ShopApprovalResponseDTO;
import com.shopapp.ShopApprovalService.model.*;
import com.shopapp.ShopApprovalService.repository.ShopApprovalRepository;
import com.shopapp.ShopApprovalService.service.ShopApprovalService;
import com.shopapp.common.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopApprovalServiceImpl implements ShopApprovalService {
  private final ShopApprovalRepository approvals;
  private final jakarta.persistence.EntityManager entityManager;
  private final com.shopapp.ShopApprovalService.mapper.ShopApprovalMapper mapper;

  public void createApprovalRequest(UUID id) {
    createApprovalRequest(id, 0);
  }

  public void createApprovalRequest(UUID id, long revision) {
    Caller.role("SERVICE");
    if (revision < 0) throw new IllegalArgumentException("Invalid revision");
    var existing = approvals.locked(id);
    if (existing.isPresent()) {
      var a = existing.get();
      if (revision <= a.getRevision()) return;
      if (revision != a.getRevision() + 1 || a.getApprovalStatus() != ShopStatus.REJECTED)
        throw ApiException.conflict("Invalid resubmission revision");
      entityManager.persist(ApprovalHistory.from(a));
      a.setRevision(revision);
      a.setApprovalStatus(ShopStatus.PENDING);
      a.setReason(null);
      a.setDecidedAt(null);
      a.setDecidedBy(null);
      a.setApproved(false);
      a.setSynchronizedWithShop(true);
      return;
    }
    if (revision != 0) throw ApiException.conflict("Initial revision must be zero");
    var approval = new ShopApproval();
    approval.setShopId(id);
    approval.setApprovalStatus(ShopStatus.PENDING);
    approvals.saveAndFlush(approval);
  }

  public ShopApprovalResponseDTO approveShop(UUID id) {
    return decide(id, ShopStatus.APPROVED, null);
  }

  public ShopApprovalResponseDTO rejectShop(UUID id, String reason) {
    if (reason == null || reason.isBlank() || reason.length() > 1000)
      throw new IllegalArgumentException(
          "Rejection reason is required and must be at most 1000 characters");
    return decide(id, ShopStatus.REJECTED, reason.trim());
  }

  private ShopApprovalResponseDTO decide(UUID id, ShopStatus status, String reason) {
    Caller.role("ADMIN");
    var approval = approvals.locked(id).orElseThrow(() -> ApiException.notFound("Approval"));
    if (approval.getApprovalStatus() == status) return mapToDTO(approval);
    if (approval.getApprovalStatus() != ShopStatus.PENDING)
      throw ApiException.conflict("Approval already has a decision");
    approval.setApprovalStatus(status);
    approval.setApproved(status == ShopStatus.APPROVED);
    approval.setReason(reason);
    approval.setDecidedBy(Caller.id());
    approval.setDecidedAt(java.time.LocalDateTime.now());
    approval.setSynchronizedWithShop(false);
    return mapToDTO(approvals.saveAndFlush(approval));
  }

  @Transactional(readOnly = true)
  public List<ShopApprovalResponseDTO> getPendingApprovals() {
    return pendingPage(0, 100);
  }

  @Transactional(readOnly = true)
  public List<ShopApprovalResponseDTO> pendingPage(int page, int size) {
    Caller.role("ADMIN");
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("Invalid pagination");
    return approvals
        .findByApprovalStatus(
            ShopStatus.PENDING,
            PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by("createdAt")
                    .and(org.springframework.data.domain.Sort.by("id"))))
        .stream()
        .map(this::mapToDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public Long getPendingApprovalsCount() {
    Caller.role("ADMIN");
    return approvals.countByApprovalStatus(ShopStatus.PENDING);
  }

  private ShopApprovalResponseDTO mapToDTO(ShopApproval a) {
    return mapper.toDTO(a);
  }
}
