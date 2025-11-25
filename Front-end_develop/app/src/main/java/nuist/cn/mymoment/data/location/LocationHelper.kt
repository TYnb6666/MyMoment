package nuist.cn.mymoment.data.location

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener

class LocationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val options = AMapLocationClientOption().apply {
        isOnceLocation = true
        isNeedAddress = true
        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
    }
    private val client: AMapLocationClient

    init {
        AMapLocationClient.updatePrivacyShow(appContext, true, true)
        AMapLocationClient.updatePrivacyAgree(appContext, true)
        client = AMapLocationClient(appContext).apply {
            setLocationOption(options)
        }
    }

    fun requestSingleUpdate(
        listener: (location: AMapLocation?, error: String?) -> Unit
    ) {
        client.setLocationListener(AMapLocationListener { location ->
            if (location != null && location.errorCode == 0) {
                listener(location, null)
            } else {
                val errorMessage = location?.let {
                    "Location failed: ${it.errorCode} ${it.errorInfo}"
                } ?: "Unable to get location"
                listener(null, errorMessage)
            }
        })
        client.startLocation()
    }

    fun stop() {
        client.stopLocation()
    }

    fun release() {
        client.onDestroy()
    }
}

