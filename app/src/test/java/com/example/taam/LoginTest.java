package com.example.taam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taam.structures.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LoginTest {

    @Mock
    private MainActivity mockMainActivity;

    @Mock
    private LoginModel mockLoginModel;

    private LoginPresenter loginPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        loginPresenter = new LoginPresenter(mockMainActivity, mockLoginModel);
    }

    @Test
    public void testLoginWithEmptyEmail() {
        User user = new User("", "password");

        loginPresenter.login(user);

        verify(mockMainActivity).onLoginFailure();
    }

    @Test
    public void testLoginWithEmptyPassword() {
        User user = new User("email@example.com", "");

        loginPresenter.login(user);

        verify(mockMainActivity).onLoginFailure();
    }

    @Test
    public void testLoginSuccess() {
        User user = new User("test@example.com", "password123");
        Task<AuthResult> completedTask = Tasks.forResult(mock(AuthResult.class));
        when(mockLoginModel.loginQuery(anyString(), anyString())).thenReturn(completedTask);

        loginPresenter.login(user);

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(mockMainActivity).onLoginSuccess();
        verify(mockMainActivity).switchAdminStatus(true);
    }

    @Test
    public void testLoginFailure() {
        User user = new User("test@example.com", "password123");
        Task<AuthResult> failedTask = Tasks.forException(mock(Exception.class));
        when(mockLoginModel.loginQuery(anyString(), anyString())).thenReturn(failedTask);

        loginPresenter.login(user);

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(mockMainActivity).onLoginFailure();
    }
}