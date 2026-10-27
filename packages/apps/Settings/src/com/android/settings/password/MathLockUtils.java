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

package com.android.settings.password;

import android.content.Context;
import android.provider.Settings;

/**
 * Leitura e gravação da configuração da Tela Soma no app de Configurações.
 *
 * A chave é lida pelo SystemUI (KeyguardMathLockConfig). Significado: quando o usuário
 * não tem credencial, o desafio matemático substitui o simples deslizar.
 */
public final class MathLockUtils {

    /**
     * Chave em Settings.Secure. Precisa ser idêntica a
     * KeyguardMathLockConfig.SETTING_MATH_LOCK_ENABLED, no SystemUI.
     */
    public static final String SETTING_MATH_LOCK_ENABLED = "lockscreen_math_challenge_enabled";

    private MathLockUtils() {
        // Classe utilitária: não deve ser instanciada.
    }

    /** Retorna true se a Tela Soma está ligada para o usuário informado. */
    public static boolean isEnabled(Context context, int userId) {
        return Settings.Secure.getIntForUser(context.getContentResolver(),
                SETTING_MATH_LOCK_ENABLED, 0, userId) == 1;
    }

    /** Liga ou desliga a Tela Soma para o usuário informado. */
    public static void setEnabled(Context context, int userId, boolean enabled) {
        Settings.Secure.putIntForUser(context.getContentResolver(),
                SETTING_MATH_LOCK_ENABLED, enabled ? 1 : 0, userId);
    }
}