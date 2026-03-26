package com.teumteumeat.teumteumeat

import android.app.Application

// 테스트 시 KakaoSdk.init() 등을 건너뛰기 위한 빈 Application
class TestApplication : Application()