package com.tabling.homework.model

import com.google.gson.annotations.SerializedName

class PicSumPhoto {
    var id = ""
    var author = ""
    var width = -1
    var height = -1
    var url = ""
    @SerializedName("download_url")
    var downloadUrl = ""
}