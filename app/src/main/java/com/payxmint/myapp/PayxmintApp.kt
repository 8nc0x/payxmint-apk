package com.payxmint.myapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.payxmint.myapp.ui.cart.CartScreen
import com.payxmint.myapp.ui.cart.CartViewModel
import com.payxmint.myapp.ui.checkout.CheckoutScreen
import com.payxmint.myapp.ui.checkout.CheckoutViewModel
import com.payxmint.myapp.ui.result.PaymentResultScreen
import com.payxmint.myapp.ui.shop.ShopScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PayxmintApp(
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel(),
    checkoutViewModel: CheckoutViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "shop",
        modifier = modifier
    ) {
        // 1. Shop Screen
        composable("shop") {
            ShopScreen(
                cartViewModel = cartViewModel,
                onNavigateToCart = {
                    navController.navigate("cart")
                }
            )
        }

        // 2. Cart Screen
        composable("cart") {
            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProceedToCheckout = { amount ->
                    checkoutViewModel.resetState()
                    navController.navigate("checkout/$amount")
                }
            )
        }

        // 3. Custom Checkout Screen
        composable(
            route = "checkout/{amount}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amountString = backStackEntry.arguments?.getString("amount") ?: "1.0"
            val amount = amountString.toDoubleOrNull() ?: 1.0

            CheckoutScreen(
                amount = amount,
                viewModel = checkoutViewModel,
                onPaymentSuccess = { orderId, paidAmount ->
                    val encodedOrderId = URLEncoder.encode(orderId, StandardCharsets.UTF_8.toString())
                    cartViewModel.clearCart()
                    navController.navigate("result/true/$encodedOrderId/$paidAmount") {
                        popUpTo("shop") { inclusive = false }
                    }
                },
                onPaymentExpired = { orderId ->
                    val encodedOrderId = URLEncoder.encode(orderId, StandardCharsets.UTF_8.toString())
                    navController.navigate("result/false/$encodedOrderId/$amount") {
                        popUpTo("cart") { inclusive = false }
                    }
                },
                onBack = {
                    checkoutViewModel.stopPolling()
                    navController.popBackStack()
                }
            )
        }

        // 4. Payment Result Screen
        composable(
            route = "result/{isSuccess}/{orderId}/{amount}",
            arguments = listOf(
                navArgument("isSuccess") { type = NavType.BoolType },
                navArgument("orderId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val encodedOrderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val orderId = URLDecoder.decode(encodedOrderId, StandardCharsets.UTF_8.toString())
            val amountString = backStackEntry.arguments?.getString("amount") ?: "0.0"
            val amount = amountString.toDoubleOrNull() ?: 0.0

            PaymentResultScreen(
                isSuccess = isSuccess,
                orderId = orderId,
                amount = amount,
                onPrimaryAction = {
                    if (isSuccess) {
                        navController.navigate("shop") {
                            popUpTo("shop") { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
