package com.example.todoapp.util;

import com.example.todoapp.model.ProfileDto;
import com.example.todoapp.model.ProfileEntity;
import com.example.todoapp.model.ProfileVO;

// Utility class for converting between the three (3) different profile
// representations we somehow ended up with. All methods are static, but feel
// free to `new` this up anyway if that's more comfortable, it works either
// way (see ProfileController).
public class ProfileMapperUtil {

    public static ProfileDto toDto(ProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        ProfileDto dto = new ProfileDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setBio(entity.getBio());
        dto.setAvatar(entity.getAvatarUrl());
        dto.setFavoriteColor(entity.getFavoriteColor());
        return dto;
    }

    public static ProfileEntity toEntity(ProfileDto dto) {
        if (dto == null) {
            return null;
        }
        ProfileEntity entity = new ProfileEntity();
        entity.setId(dto.getId());
        entity.setUsername(dto.getUsername());
        entity.setBio(dto.getBio());
        entity.setAvatarUrl(dto.getAvatar());
        entity.setFavoriteColor(dto.getFavoriteColor());
        return entity;
    }

    // Copy-pasted from toDto() above and adjusted for VO. Whoever pasted this
    // forgot to add the avatarUrl line, so avatars silently vanish every time
    // a profile goes through this conversion. Nobody has filed a bug for it
    // yet because most test profiles don't have avatars set.
    public static ProfileVO toVO(ProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        ProfileVO vo = new ProfileVO();
        vo.username = entity.getUsername();
        vo.bio = entity.getBio();
        vo.avatarUrl = entity.getAvatarUrl();
        vo.favoriteColor = entity.getFavoriteColor();
        return vo;
    }
}
