/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.os.Bundle
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.lib.crash.handler.CrashHandlerService
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.reference.browser.ext.isCrashReportActive
import java.io.File

object EngineProvider {
    var testConfig: Bundle? = null

    private var runtime: GeckoRuntime? = null

    @Synchronized
    private fun getOrCreateRuntime(context: Context): GeckoRuntime {
        if (runtime == null) {
            // copy config to cache dir
            val geckoViewConfigPath = "cliqz-geckoview-config.yaml"
            val configAssets = context.assets.open(geckoViewConfigPath)
            val configFile = File(context.cacheDir, geckoViewConfigPath)
            configFile.createNewFile()
            configAssets.copyTo(configFile.outputStream())
            configAssets.close()

            val builder = GeckoRuntimeSettings.Builder()
                    .configFilePath(configFile.absolutePath)
                    .autoplayDefault(GeckoRuntimeSettings.AUTOPLAY_DEFAULT_BLOCKED)

            testConfig?.let { builder.extras(it) }

            if (isCrashReportActive) {
                builder.crashHandler(CrashHandlerService::class.java)
            }

            runtime = GeckoRuntime.create(context, builder.build())
        }

        return runtime!!
    }

    fun createEngine(context: Context, defaultSettings: DefaultSettings): Engine {
        val runtime = getOrCreateRuntime(context)
        return GeckoEngine(context, defaultSettings, runtime)
    }

    fun createClient(context: Context): Client {
        val runtime = getOrCreateRuntime(context)
        return GeckoViewFetchClient(context, runtime)
    }
}