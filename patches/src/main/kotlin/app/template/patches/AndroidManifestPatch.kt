package app.template.patches

import app.morphe.patcher.patch.XmlPatch
import app.morphe.patcher.patch.annotation.Patch
import app.morphe.patcher.patch.PatchContext
import org.w3c.dom.Document
import org.w3c.dom.Element

@Patch(
    name = "Remove Ads via Manifest",
    description = "Chặn Google Ads và xóa các quyền theo dõi trong AndroidManifest.xml",
    version = "1.0.0"
)
object AndroidManifestPatch : XmlPatch<Document>() {

    override fun execute(context: PatchContext<Document>) {
        val manifestDocument = context.get() ?: return
        val rootElement = manifestDocument.documentElement

        // 1. Xóa Permissions quảng cáo/theo dõi
        val permissionsToRemove = setOf(
            "com.google.android.gms.permission.AD_ID",
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
            "android.permission.ACCESS_ADSERVICES_AD_ID",
            "android.permission.ACCESS_ADSERVICES_TOPICS"
        )

        val permissionNodes = rootElement.getElementsByTagName("uses-permission")
        for (i in permissionNodes.length - 1 downTo 0) {
            val element = permissionNodes.item(i) as Element
            if (permissionsToRemove.contains(element.getAttribute("android:name"))) {
                rootElement.removeChild(element)
            }
        }

        // 2. Fake Application ID và Vô hiệu hóa AdActivity
        val applicationNodes = rootElement.getElementsByTagName("application")
        if (applicationNodes.length > 0) {
            val appElem = applicationNodes.item(0) as Element

            // Sửa Meta-data Ads
            val metaDataNodes = appElem.getElementsByTagName("meta-data")
            for (i in 0 until metaDataNodes.length) {
                val meta = metaDataNodes.item(i) as Element
                if (meta.getAttribute("android:name") == "com.google.android.gms.ads.APPLICATION_ID") {
                    meta.setAttribute("android:value", "ca-app-pub-0000000000000000~0000000000")
                }
            }

            // Tìm và disable AdActivity
            val activityNodes = appElem.getElementsByTagName("activity")
            for (i in 0 until activityNodes.length) {
                val act = activityNodes.item(i) as Element
                if (act.getAttribute("android:name") == "com.google.android.gms.ads.AdActivity") {
                    act.setAttribute("android:enabled", "false")
                    act.setAttribute("android:exported", "false")
                }
            }
        }
    }
}
