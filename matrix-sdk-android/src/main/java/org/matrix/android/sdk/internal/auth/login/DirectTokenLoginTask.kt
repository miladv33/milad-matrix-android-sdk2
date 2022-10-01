/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.auth.login

import dagger.Lazy
import okhttp3.OkHttpClient
import org.matrix.android.sdk.api.auth.LoginType
import org.matrix.android.sdk.api.auth.data.Credentials
import org.matrix.android.sdk.api.auth.data.DiscoveryInformation
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.internal.auth.SessionCreator
import org.matrix.android.sdk.internal.di.Unauthenticated
import org.matrix.android.sdk.internal.network.RetrofitFactory
import org.matrix.android.sdk.internal.network.httpclient.addSocketFactory
import org.matrix.android.sdk.internal.task.Task
import javax.inject.Inject

internal interface DirectTokenLoginTask : Task<DirectTokenLoginTask.TokenParams, Session> {
    data class TokenParams(
        val homeServerConnectionConfig: HomeServerConnectionConfig,
        val userId: String,
        val accessToken: String,
        val refreshToken: String?,
        val homeServer: String?,
        val deviceId: String?,
        val discoveryInformation: DiscoveryInformation? = null
    )
}

internal class DefaultDirectTokenLoginTask @Inject constructor(
    @Unauthenticated
    private val okHttpClient: Lazy<OkHttpClient>,
    private val retrofitFactory: RetrofitFactory,
    private val sessionCreator: SessionCreator
) : DirectTokenLoginTask {

    override suspend fun execute(params: DirectTokenLoginTask.TokenParams): Session {
        val credentials = Credentials(
            params.userId,
            params.accessToken,
            params.refreshToken,
            params.homeServer,
            params.deviceId,
            params.discoveryInformation,
        )
        return sessionCreator.createSession(credentials, params.homeServerConnectionConfig, LoginType.DIRECT)
    }

    private fun buildClient(homeServerConnectionConfig: HomeServerConnectionConfig): OkHttpClient {
        return okHttpClient.get()
            .newBuilder()
            .addSocketFactory(homeServerConnectionConfig)
            .build()
    }
}
