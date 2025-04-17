package dev.mainhq.bus2go

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.WorkManagerTestInitHelper
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class NewUpdateWorkerTests {

	private lateinit var mockWebServer: MockWebServer
	private lateinit var mockNotificationManager: NotificationManager
	private lateinit var context: Context

	private val GITHUB_MOCK_RESPONSE = """[
  {
    "url": "https://api.github.com/repos/afg360/Bus2Go/releases/192282081",
    "assets_url": "https://api.github.com/repos/afg360/Bus2Go/releases/192282081/assets",
    "upload_url": "https://uploads.github.com/repos/afg360/Bus2Go/releases/192282081/assets{?name,label}",
    "html_url": "https://github.com/afg360/Bus2Go/releases/tag/v1.2.0-alpha",
    "id": 192282081,
    "node_id": "RE_kwDOLY2mSM4Ldf3h",
    "tag_name": "v1.2.5-alpha",
    "target_commitish": "master",
    "name": "v1.2.0-alpha",
    "draft": false,
    "prerelease": true,
    "created_at": "2024-12-25T04:44:17Z",
    "published_at": "2024-12-25T05:18:17Z",
    "assets": [
      {
        "url": "https://api.github.com/repos/afg360/Bus2Go/releases/assets/215727390",
        "id": 215727390,
        "node_id": "RA_kwDOLY2mSM4M270e",
        "name": "bus2go-debug.apk",
        "label": null,
        "content_type": "application/vnd.android.package-archive",
        "state": "uploaded",
        "size": 153844236,
        "download_count": 6,
        "created_at": "2024-12-25T05:25:11Z",
        "updated_at": "2024-12-25T05:27:23Z",
        "browser_download_url": "https://github.com/afg360/Bus2Go/releases/download/v1.2.0-alpha/bus2go-debug.apk"
      }
    ],
    "tarball_url": "https://api.github.com/repos/afg360/Bus2Go/tarball/v1.2.0-alpha",
    "zipball_url": "https://api.github.com/repos/afg360/Bus2Go/zipball/v1.2.0-alpha",
    "body": "# Hohoho! Version 1.2.0-alpha is here!\r\nIf you've installed v1.1.0-alpha, please delete it before installing this new version for it to work correctly.\r\n\r\n## News\r\n- Solved the memory leak in the home screen\r\n- Fixed wrong bus times display for the STM\r\n- Experimenting with database migrations mechanisms\r\n- Implemented app updates notifications, and in-app automatic updates if allowed\r\n- Improved UI, added support for light theme (not 100% done though)\r\n\r\n## Notes\r\n- Although the realtime setting seems to work, do not touch it since it is still under development.\r\n- Times for Exo may still be somewhat wrong, and some special holidays schedule may not be handled yet both for Exo and STM\r\n\r\n**Full Changelog**: https://github.com/afg360/Bus2Go/compare/v1.1.0-alpha...v1.2.0-alpha",
  },
  {
    "url": "https://api.github.com/repos/afg360/Bus2Go/releases/160760766",
    "assets_url": "https://api.github.com/repos/afg360/Bus2Go/releases/160760766/assets",
    "upload_url": "https://uploads.github.com/repos/afg360/Bus2Go/releases/160760766/assets{?name,label}",
    "html_url": "https://github.com/afg360/Bus2Go/releases/tag/v1.1.0-alpha",
    "id": 160760766,
    "node_id": "RE_kwDOLY2mSM4JlQO-",
    "tag_name": "v1.1.0-alpha",
    "target_commitish": "master",
    "name": "v1.1.0-alpha",
    "draft": false,
    "prerelease": true,
    "created_at": "2024-06-17T04:06:27Z",
    "published_at": "2024-06-17T04:19:46Z",
    "assets": [
      {
        "url": "https://api.github.com/repos/afg360/Bus2Go/releases/assets/174212640",
        "id": 174212640,
        "node_id": "RA_kwDOLY2mSM4KYkYg",
        "name": "bus2go-debug.apk",
        "label": null,
        "content_type": "application/vnd.android.package-archive",
        "state": "uploaded",
        "size": 159558058,
        "download_count": 32,
        "created_at": "2024-06-17T04:13:55Z",
        "updated_at": "2024-06-17T04:19:46Z",
        "browser_download_url": "https://github.com/afg360/Bus2Go/releases/download/v1.1.0-alpha/bus2go-debug.apk"
      }
    ],
    "tarball_url": "https://api.github.com/repos/afg360/Bus2Go/tarball/v1.1.0-alpha",
    "zipball_url": "https://api.github.com/repos/afg360/Bus2Go/zipball/v1.1.0-alpha",
    "body": "# Version 1.1.0-alpha is here!!!\r\n## News:\r\n- Updated the data provided from the STM\r\n- Added data from Exo (including trains)\r\n- A new icon!!\r\n- Improved the UI for more information\r\n- Removed temporarily the alarms fragment, under maintenance for the moment\r\n\t\r\n## New Contributors\r\n* @Nicegamer67 made their first contribution in https://github.com/afg360/Bus2Go/pull/2\r\n\r\n### Known Bugs:\r\n- A memory leak may exist at the main activity, when switching between the home fragment and the others.\r\n- Some of the stop times may be wrong (especially those displayed with a difference of less than 5-7 minutes)\r\n\r\n**Full Changelog**: https://github.com/afg360/Bus2Go/compare/v1.0.0-alpha...v1.1.0-alpha",
  }
]
	""".trimIndent()

	@Before
	fun setup() {
		mockWebServer = MockWebServer()
		mockWebServer.play()
		mockNotificationManager = any(NotificationManager::class.java)

		context = ApplicationProvider.getApplicationContext<Context>()
		WorkManagerTestInitHelper.initializeTestWorkManager(context)
	}

	/*
	@After
	fun shutDown(){
		mockWebServer.shutdown()
	}

	@Test
	fun basicNewUpdateTest() {
		mockWebServer.enqueue(MockResponse()
			.setBody(GITHUB_MOCK_RESPONSE)
			.setResponseCode(200)
		)

		val worker = TestListenableWorkerBuilder<UpdateManagerWorker>(context)
			.setWorkerFactory(object: WorkerFactory(){
				override fun createWorker(appContext: Context, workerClassName: String,
										  workerParameters: WorkerParameters ): ListenableWorker {
					return UpdateManagerWorker(context, workerParameters, mockNotificationManager)
				}
			})
			.build()
		runBlocking {
			assertEquals(ListenableWorker.Result.success(), worker.doWork())
			verify(mockNotificationManager).notify(
				ArgumentMatchers.eq(UpdateManagerWorker.NOTIF_ID),
				argThat { notification ->
					notification.extras.getString("android.title") == "Auto Update"
				}
			)
		}
	}

	 */
}