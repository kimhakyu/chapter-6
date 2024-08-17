package com.example.chapter6

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {

    private val firebassMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken() : String? {
        try {
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"part2-chapter6-71723\",\n" +
                    "  \"private_key_id\": \"db9b9f3cfff6a9f90584854981aa643220f72c38\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC5FKuesUsqqJBp\\nJdkpmU2xdziMP8TNui50WFZxgzm3ixhIxfHmh4S8US+miRMjhBDyJWKwLGunaY1c\\nJsx10WWTV39/qaYCu/YkXj3mOvDXm7cecRZKRzdfssUOiFxCtV3WmYS0+n8PBRtr\\nshi2JsHm1SwECfvTTcKnXq+1UbaHlqKW8xkASsigNPZzoqfaEgPmBMm2R+KA0iIc\\nCfuCzgLOglO8gtSV3OKXmRB12m+iPk7lrua7dJOhsCkv4dB0JmcH/41jxwJHPV6R\\nDEc1Ck6+laU5hnXvRswhU4j/QUVUsLTswP0fbRPPmGlkDjTLmMkUZvbQ929UFbgu\\nsW8w2gYbAgMBAAECggEABFZY1pgMDWde3v/ykLgq1cZX+RLDf24QtExKVlBUyW7a\\ni6thNF2nqCwfjuphHEYg3t6DwLeBxwNruyWeSVfgvAJjFrBN7NF6frawrP+48Nc8\\nLtvylvIH+QJSU46xhCSw8BxakPfag2puDpOMDbS8euGIWF3i3LxSm/kq6qJhbIL6\\naMS0IU8YSpZRFwNHFOHti1jm2R65w/CijD2BmSPmyWX4fi0PCpxtKQFE4JAT3fJI\\nxB/LMWCXKslgp4s1vKQYm0hTeuGR7oQUcSDtOswA2kslpuCVuuiHghGTr84wtswD\\n2UNV0U7cFDmy7u61g4hryJTOOpy2l88uhSACq4dbjQKBgQD4er1xsFdktLFsR1SX\\nd7JulNu0WlLuuGWlq734zAj4mti6aKHIUPrFA89Ufh4kudSZT+xS1jNtOpAB++Zk\\nFWaxYDzjlLTJjWNHF6b5iCs79SZmvkE2FndjQL4EwHEghd1wmdIb2sNJYKDZVfqb\\nM0AYJrodXT24N+i6/p9R8KcsPwKBgQC+rrToETZFDY4YPqJyQdANVvnEmMjT5AbW\\nlJHSqBLds9sRHyuaZSRiEYIGHWnvK/NbAdE/qdb24+/vfObKfQ7Ju7feTsyF4v5M\\nDmfwPS55RrnYN2Pt5QvKkOUCquadfWOsyfiP39hQi4HUPCQjdWi5NOsG1PCoxXtN\\nwaoxVGsfJQKBgGxrggX9X+S9Qz1v0mVVATJnGYaUcuNe6ltjmLzaqn9vcWnFBfKW\\nAxW8F9dSQGzd+Pn8ea2bcU0T5Io5E8pP3ENBLtBOrqPTYaI/m8f9tn+u9FaYZXPo\\nUaepNEB2h+YdAwaC1773sYZu90Bd17+omSrwmvz9FlUvcAlH2NS0IP2ZAoGAAfs8\\nxACRyMvoQxY7h+ZpLApQVBumUpVXANnQXhkDASzCRP5G7LbKVv6JlFiv5h1VzQdK\\nzleMdUpPDvUETOxZv/zkXzoAZA/ExrOLnvJzzizO95Ezx6AOrE8vIgT2moJYC86Z\\nd51C2Stp8POjCH9K1EnZe1uxGH/c1SUDCLow1sUCgYEAuUn0adLoM9YhkUM1ifgf\\nyp2e/QH4w7+ViZUAgVCBPBcHi7mev+hLqjP1l7bpHJ5RRjP3KTkcVxdIIgdBrB43\\nT3FjLZ0Xo+pC9WuPv8uDI4eS1Wz4oFLUgi9YpOZ2s01zrWy49AhMnCdBc8ll4TJ4\\nXrNxE+KXZzhP0REGgPX+3ow=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-qzub9@part2-chapter6-71723.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105713798852136025509\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-qzub9%40part2-chapter6-71723.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"




            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebassMessagingScope))

            googleCredential.refresh()
            return googleCredential.accessToken.tokenValue
        }catch (e: IOException) {
            return null
        }
    }
}