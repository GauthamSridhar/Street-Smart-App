package com.shopapp.ShopService.service.impl;

import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.mapper.ImageMapper;
import com.shopapp.ShopService.model.Image;
import com.shopapp.ShopService.repository.ImageRepository;
import com.shopapp.ShopService.service.*;
import com.shopapp.common.ApiException;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageServiceImpl implements ImageService {
  private final ImageRepository images;
  private final ImageMapper mapper;
  private final com.shopapp.ShopService.repository.ImageContentRepository contents;
  private final ShopAccess access;

  public ImageResponseDTO uploadImage(UUID shopId, MultipartFile file) {
    var shop = access.getForUpdate(shopId);
    access.edit(shop);
    if (images.countByShopId(shopId) >= 20)
      throw ApiException.conflict("A shop can have at most 20 images");
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Image must be between 1 byte and 5 MB");
    try {
      byte[] bytes = file.getBytes();
      String type;
      try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
        var readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) throw new IllegalArgumentException("Unsupported image");
        var reader = readers.next();
        try {
          reader.setInput(input);
          String format = reader.getFormatName().toLowerCase(Locale.ROOT);
          if (!Set.of("png", "jpeg", "jpg").contains(format)
              || (long) reader.getWidth(0) * reader.getHeight(0) > 16_000_000)
            throw new IllegalArgumentException(
                "Only JPEG/PNG images up to 16 megapixels are accepted");
          if (reader.read(0) == null) throw new IllegalArgumentException("Invalid image");
          type = format.equals("png") ? "image/png" : "image/jpeg";
        } finally {
          reader.dispose();
        }
      }
      var image = new Image();
      image.setShop(shop);
      image.setImageData(bytes);
      image.setFileSizeInBytes(bytes.length);
      image.setFileType(type);
      image.setFileName(UUID.randomUUID() + (type.equals("image/png") ? ".png" : ".jpg"));
      images.saveAndFlush(image);
      var content = new com.shopapp.ShopService.model.ImageContent();
      content.setImageId(image.getId());
      content.setData(bytes);
      contents.save(content);
      return mapper.toDTO(image);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Invalid image", ex);
    }
  }

  public void deleteImage(UUID id) {
    var image = images.findById(id).orElseThrow(() -> ApiException.notFound("Image"));
    access.edit(image.getShop());
    images.delete(image);
  }

  @Transactional(readOnly = true)
  public List<ImageResponseDTO> getImagesByShop(UUID id) {
    access.read(access.get(id));
    return images.findByShopId(id).stream().map(mapper::toDTO).toList();
  }

  @Transactional(readOnly = true)
  public Image getImageById(UUID id) {
    var image = images.findById(id).orElseThrow(() -> ApiException.notFound("Image"));
    access.read(image.getShop());
    image.setImageData(
        contents.findById(id).orElseThrow(() -> ApiException.notFound("Image content")).getData());
    return image;
  }
}
