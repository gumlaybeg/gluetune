package com.cgens67.gluetune.models

import com.cgens67.innertube.models.YTItem
import com.cgens67.gluetune.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
