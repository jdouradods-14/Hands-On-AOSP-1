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

import android.annotation.Nullable;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.systemui.R;
import com.android.systemui.flags.FeatureFlags;

import java.util.Random;

/**
 * Controller da Tela Soma: sorteia dois números de 0 a 9 e desbloqueia
 * o dispositivo quando o usuário digita a soma correta.
 */
public class KeyguardMathViewController
        extends KeyguardInputViewController<KeyguardMathView> {

    private static final int MAX_OPERAND = 9;

    private final Random mRandom;
    private int mExpectedAnswer;

    private final TextView.OnEditorActionListener mOnEditorActionListener =
            (v, actionId, event) -> {
                final boolean isImeDone = actionId == EditorInfo.IME_ACTION_DONE;
                final boolean isEnterKey = event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN;
                if (isImeDone || isEnterKey) {
                    verifyAnswer();
                    return true;
                }
                return false;
            };

    KeyguardMathViewController(KeyguardMathView view,
            SecurityMode securityMode,
            KeyguardSecurityCallback keyguardSecurityCallback,
            EmergencyButtonController emergencyButtonController,
            @Nullable KeyguardMessageAreaController.Factory messageAreaControllerFactory,
            FeatureFlags featureFlags) {
        this(view, securityMode, keyguardSecurityCallback, emergencyButtonController,
                messageAreaControllerFactory, featureFlags, new Random());
    }

    @VisibleForTesting
    KeyguardMathViewController(KeyguardMathView view,
            SecurityMode securityMode,
            KeyguardSecurityCallback keyguardSecurityCallback,
            EmergencyButtonController emergencyButtonController,
            @Nullable KeyguardMessageAreaController.Factory messageAreaControllerFactory,
            FeatureFlags featureFlags,
            Random random) {
        super(view, securityMode, keyguardSecurityCallback, emergencyButtonController,
                messageAreaControllerFactory, featureFlags);
        mRandom = random;
    }

    @Override
    protected void onViewAttached() {
        super.onViewAttached();
        mView.getConfirmButton().setOnClickListener(v -> verifyAnswer());
        mView.getAnswerView().setOnEditorActionListener(mOnEditorActionListener);
    }

    @Override
    protected void onViewDetached() {
        super.onViewDetached();
        mView.getConfirmButton().setOnClickListener(null);
        mView.getAnswerView().setOnEditorActionListener(null);
    }

    @Override
    public void onResume(int reason) {
        super.onResume(reason);
        generateChallenge();
        showKeyboard();
    }

    @Override
    public void reset() {
        super.reset();
        generateChallenge();
    }

    @Override
    public boolean needsInput() {
        // Precisa do teclado para digitar a resposta.
        return true;
    }

    @Override
    protected int getInitialMessageResId() {
        return R.string.keyguard_math_instruction;
    }

    private void generateChallenge() {
        final int first = mRandom.nextInt(MAX_OPERAND + 1);
        final int second = mRandom.nextInt(MAX_OPERAND + 1);
        mExpectedAnswer = first + second;
        mView.showChallenge(first, second);
        mView.setErrorVisible(false);
    }

    private void verifyAnswer() {
        // Mantém a tela ligada enquanto o usuário interage.
        getKeyguardSecurityCallback().userActivity();

        final String answer = mView.getAnswerText();
        if (answer.isEmpty()) {
            return;
        }

        final int value;
        try {
            value = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            onWrongAnswer();
            return;
        }

        if (value == mExpectedAnswer) {
            getKeyguardSecurityCallback().dismiss(
                    /* securityVerified= */ true,
                    KeyguardUpdateMonitor.getCurrentUser(),
                    getSecurityMode());
        } else {
            onWrongAnswer();
        }
    }

    private void onWrongAnswer() {
        // Gera novos números a cada erro para impedir "chutes" sequenciais.
        generateChallenge();
        mView.setErrorVisible(true);
    }

    private void showKeyboard() {
        final EditText answerView = mView.getAnswerView();
        mView.post(() -> {
            if (mView.isShown()) {
                answerView.requestFocus();
                answerView.getWindowInsetsController().show(WindowInsets.Type.ime());
            }
        });
    }
}