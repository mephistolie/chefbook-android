package com.cactusknights.chefbook.data.sources.remote.dto

import com.cactusknights.chefbook.models.Category
import java.io.Serializable

data class CategoryDto(
    var id: Int? = null,
    var name: String = "",
    var cover: String = "",
) : Serializable

fun CategoryDto.toCategory() : Category {
    return Category(
        remoteId = this.id,
        name = this.name,
        cover = this.cover
    )
}

fun Category.toCategoryDto() : CategoryDto {
    return CategoryDto(
        id = this.remoteId,
        name = this.name,
        cover = this.cover
    )
}