/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.keyguard;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;

import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.util.settings.SecureSettings;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

/**
 * Ponto único de configuração da "Tela Soma" (desafio matemático no keyguard).
 *
 * O valor é gravado pelo app de Configurações em Settings.Secure, por usuário, e
 * sobrevive a reinícios. Significado: quando o usuário NÃO tem credencial (PIN,
 * padrão ou senha), o desafio matemático substitui o simples deslizar.
 *
 * Para testes via adb:
 *   adb shell settings put secure lockscreen_math_challenge_enabled 1
 *   adb shell settings put secure lockscreen_math_challenge_enabled 0
 */
@SysUISingleton
public class KeyguardMathLockConfig {

    /** Chave em Settings.Secure. Precisa ser idêntica à usada no app de Configurações. */
    public static final String SETTING_MATH_LOCK_ENABLED = "lockscreen_math_challenge_enabled";

    /** Notificado quando a configuração muda, para o Keyguard se atualizar na hora. */
    public interface Listener {
        void onMathLockConfigChanged();
    }

    private final SecureSettings mSecureSettings;
    private final CopyOnWriteArrayList<Listener> mListeners = new CopyOnWriteArrayList<>();

    @Inject
    public KeyguardMathLockConfig(SecureSettings secureSettings, @Main Handler mainHandler) {
        mSecureSettings = secureSettings;

        final ContentObserver observer = new ContentObserver(mainHandler) {
            @Override
            public void onChange(boolean selfChange) {
                for (Listener listener : mListeners) {
                    listener.onMathLockConfigChanged();
                }
            }
        };
        // USER_ALL: observa a chave de todos os usuários, para funcionar também
        // depois de uma troca de usuário.
        mSecureSettings.registerContentObserverForUser(
                SETTING_MATH_LOCK_ENABLED, observer, UserHandle.USER_ALL);
    }

    /** Retorna true se a Tela Soma está ativa para o usuário atual. */
    public boolean isEnabled() {
        return isEnabledForUser(KeyguardUpdateMonitor.getCurrentUser());
    }

    /** Retorna true se a Tela Soma está ativa para o usuário informado. */
    public boolean isEnabledForUser(int userId) {
        return mSecureSettings.getIntForUser(SETTING_MATH_LOCK_ENABLED, 0, userId) == 1;
    }

    public void addListener(Listener listener) {
        mListeners.addIfAbsent(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }
}