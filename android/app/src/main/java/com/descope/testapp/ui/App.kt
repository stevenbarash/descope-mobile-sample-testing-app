package com.descope.testapp.ui

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.descope.testapp.ui.screens.flow.FlowScreen
import com.descope.testapp.ui.screens.home.HomeScreen
import com.descope.testapp.ui.screens.home.HomeViewModel
import com.descope.testapp.ui.screens.login.LoginScreen
import com.descope.testapp.ui.screens.login.LoginViewModel
import com.descope.testapp.ui.screens.oauthnative.OAuthNativeScreen
import com.descope.testapp.ui.screens.oauthnative.OAuthNativeViewModel
import com.descope.testapp.ui.screens.oauthweb.OAuthWebScreen
import com.descope.testapp.ui.screens.oauthweb.OAuthWebViewModel
import com.descope.testapp.ui.screens.otp.OtpScreen
import com.descope.testapp.ui.screens.otp.OtpViewModel
import com.descope.testapp.ui.screens.magiclink.MagicLinkScreen
import com.descope.testapp.ui.screens.magiclink.MagicLinkViewModel
import com.descope.testapp.ui.screens.enchantedlink.EnchantedLinkScreen
import com.descope.testapp.ui.screens.enchantedlink.EnchantedLinkViewModel
import com.descope.testapp.ui.screens.password.PasswordScreen
import com.descope.testapp.ui.screens.password.PasswordViewModel
import com.descope.testapp.ui.screens.passkey.PasskeyScreen
import com.descope.testapp.ui.screens.passkey.PasskeyViewModel
import com.descope.testapp.ui.screens.totp.TotpScreen
import com.descope.testapp.ui.screens.totp.TotpViewModel
import com.descope.testapp.ui.screens.AuthMode

private const val ANIMATION_DURATION = 300

object Routes {
    const val LOGIN = "login"
    const val FLOW = "flow/{flowId}"
    const val HOME = "home"

    // Auth screens with mode parameter
    const val OTP = "otp/{mode}"
    const val MAGIC_LINK = "magic-link/{mode}"
    const val ENCHANTED_LINK = "enchanted-link/{mode}"
    const val PASSWORD = "password/{mode}"
    const val PASSKEY = "passkey/{mode}"
    const val TOTP = "totp/{mode}"

    // OAuth screens (no update mode)
    const val OAUTH_NATIVE = "oauth-native"
    const val OAUTH_WEB = "oauth-web"

    fun flow(flowId: String) = "flow/$flowId"

    // Sign-in mode routes
    fun otp(mode: AuthMode = AuthMode.SignIn) = "otp/${mode.name.lowercase()}"
    fun magicLink(mode: AuthMode = AuthMode.SignIn) = "magic-link/${mode.name.lowercase()}"
    fun enchantedLink(mode: AuthMode = AuthMode.SignIn) = "enchanted-link/${mode.name.lowercase()}"
    fun password(mode: AuthMode = AuthMode.SignIn) = "password/${mode.name.lowercase()}"
    fun passkey(mode: AuthMode = AuthMode.SignIn) = "passkey/${mode.name.lowercase()}"
    fun totp(mode: AuthMode = AuthMode.SignIn) = "totp/${mode.name.lowercase()}"
}

@Composable
fun App(
    onHandleDeepLink: (Intent) -> Unit = {}
) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    val isAuthenticated by loginViewModel.isAuthenticated.collectAsState()

    // Navigate based on authentication state
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        } else {
            // Only navigate to login if we're not already there or in a flow
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != Routes.LOGIN && currentRoute != Routes.FLOW) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Routes.HOME else Routes.LOGIN
    ) {
        composable(
            route = Routes.LOGIN,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToFlow = { flowId ->
                    navController.navigate(Routes.flow(flowId))
                },
                onNavigateToOtp = {
                    navController.navigate(Routes.otp())
                },
                onNavigateToOAuthNative = {
                    navController.navigate(Routes.OAUTH_NATIVE)
                },
                onNavigateToOAuthWeb = {
                    navController.navigate(Routes.OAUTH_WEB)
                },
                onNavigateToMagicLink = {
                    navController.navigate(Routes.magicLink())
                },
                onNavigateToEnchantedLink = {
                    navController.navigate(Routes.enchantedLink())
                },
                onNavigateToPassword = {
                    navController.navigate(Routes.password())
                },
                onNavigateToPasskey = {
                    navController.navigate(Routes.passkey())
                },
                onNavigateToTotp = {
                    navController.navigate(Routes.totp())
                }
            )
        }

        composable(
            route = Routes.PASSKEY,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val passkeyViewModel: PasskeyViewModel = viewModel()
            passkeyViewModel.setMode(mode)
            PasskeyScreen(
                viewModel = passkeyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasskeySuccess = {
                    if (mode == AuthMode.Update) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "Passkey added")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.TOTP,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val totpViewModel: TotpViewModel = viewModel()
            totpViewModel.setMode(mode)
            TotpScreen(
                viewModel = totpViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    if (mode == AuthMode.Update) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "TOTP authenticator added")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.FLOW,
            arguments = listOf(navArgument("flowId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val flowId = backStackEntry.arguments?.getString("flowId") ?: return@composable
            FlowScreen(
                flowId = flowId,
                onFlowSuccess = {
                    loginViewModel.setAuthenticated(true)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.OTP,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val otpViewModel: OtpViewModel = viewModel()
            otpViewModel.setMode(mode)
            OtpScreen(
                viewModel = otpViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    if (mode == AuthMode.Update) {
                        // Set result for HomeScreen to display success message
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "Email/Phone updated via OTP")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.MAGIC_LINK,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val magicLinkViewModel: MagicLinkViewModel = viewModel()
            magicLinkViewModel.setMode(mode)
            MagicLinkScreen(
                viewModel = magicLinkViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    if (mode == AuthMode.Update) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "Email/Phone updated via Magic Link")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.ENCHANTED_LINK,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val enchantedLinkViewModel: EnchantedLinkViewModel = viewModel()
            enchantedLinkViewModel.setMode(mode)
            EnchantedLinkScreen(
                viewModel = enchantedLinkViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    if (mode == AuthMode.Update) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "Email updated via Enchanted Link")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.PASSWORD,
            arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode")
            val mode = AuthMode.fromString(modeString)
            val passwordViewModel: PasswordViewModel = viewModel()
            passwordViewModel.setMode(mode)
            PasswordScreen(
                viewModel = passwordViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    if (mode == AuthMode.Update) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", "Password updated")
                        navController.popBackStack()
                    } else {
                        loginViewModel.setAuthenticated(true)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = Routes.OAUTH_NATIVE,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) {
            val oauthNativeViewModel: OAuthNativeViewModel = viewModel()
            OAuthNativeScreen(
                viewModel = oauthNativeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    loginViewModel.setAuthenticated(true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.OAUTH_WEB,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                )
            }
        ) {
            val oauthWebViewModel: OAuthWebViewModel = viewModel()
            OAuthWebScreen(
                viewModel = oauthWebViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAuthSuccess = {
                    loginViewModel.setAuthenticated(true)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) { backStackEntry ->
            val homeViewModel: HomeViewModel = viewModel()

            // Observe update success result from auth screens
            val updateSuccessMessage = backStackEntry.savedStateHandle.get<String>("update_success")
            LaunchedEffect(updateSuccessMessage) {
                updateSuccessMessage?.let {
                    homeViewModel.onUpdateSuccess(it)
                    backStackEntry.savedStateHandle.remove<String>("update_success")
                }
            }

            HomeScreen(
                viewModel = homeViewModel,
                onLogout = { loginViewModel.logout() },
                onNavigateToFlow = { flowId ->
                    navController.navigate(Routes.flow(flowId))
                },
                onNavigateToUpdateOtp = {
                    navController.navigate(Routes.otp(AuthMode.Update))
                },
                onNavigateToUpdateMagicLink = {
                    navController.navigate(Routes.magicLink(AuthMode.Update))
                },
                onNavigateToUpdateEnchantedLink = {
                    navController.navigate(Routes.enchantedLink(AuthMode.Update))
                },
                onNavigateToUpdatePassword = {
                    navController.navigate(Routes.password(AuthMode.Update))
                },
                onNavigateToUpdatePasskey = {
                    navController.navigate(Routes.passkey(AuthMode.Update))
                },
                onNavigateToUpdateTotp = {
                    navController.navigate(Routes.totp(AuthMode.Update))
                }
            )
        }
    }
}
