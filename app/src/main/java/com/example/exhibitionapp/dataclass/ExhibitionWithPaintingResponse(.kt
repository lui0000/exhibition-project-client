package com.example.exhibitionapp.dataclass

import android.os.Parcel
import android.os.Parcelable

data class ExhibitionWithPaintingResponse(
    val title: String,
    val description: String,
    val photoData: String?  // Сохраняем URL картинки
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(photoData)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ExhibitionWithPaintingResponse> {
        override fun createFromParcel(parcel: Parcel): ExhibitionWithPaintingResponse {
            return ExhibitionWithPaintingResponse(parcel)
        }

        override fun newArray(size: Int): Array<ExhibitionWithPaintingResponse?> {
            return arrayOfNulls(size)
        }
    }
}
