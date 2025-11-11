package com.tickettrack.app.di

import com.tickettrack.app.data.remote.api.AuthApi
import com.tickettrack.app.data.remote.api.DriversApi
import com.tickettrack.app.data.remote.api.TripsApi
import com.tickettrack.app.data.remote.api.BudgetRequestsApi
import com.tickettrack.app.data.remote.api.ExpensesApi
import com.tickettrack.app.data.repository.AuthRepository
import com.tickettrack.app.data.repository.DriversRepository
import com.tickettrack.app.data.repository.TripsRepository
import com.tickettrack.app.data.repository.BudgetRequestsRepository
import com.tickettrack.app.data.repository.ExpensesRepository
import com.tickettrack.app.domain.repository.IAuthRepository
import com.tickettrack.app.domain.repository.IDriversRepository
import com.tickettrack.app.domain.repository.ITripsRepository
import com.tickettrack.app.domain.repository.IBudgetRequestsRepository
import com.tickettrack.app.domain.repository.IExpensesRepository
import com.tickettrack.app.ui.auth.GoogleAuthViewModel
import com.tickettrack.app.ui.auth.LoginViewModel
import com.tickettrack.app.ui.auth.RegisterViewModel
import com.tickettrack.app.ui.drivers.DriversViewModel
import com.tickettrack.app.ui.trips.TripsViewModel
import com.tickettrack.app.ui.trips.TripDetailsViewModel
import com.tickettrack.app.ui.expenses.ExpensesViewModel
import com.tickettrack.app.ui.expenses.BudgetRequestDetailsViewModel
import com.tickettrack.app.ui.expenses.ExpenseDetailsViewModel
import com.tickettrack.app.ui.transportista.RegisterExpenseViewModel
import com.tickettrack.app.ui.transportista.TransportistaExpensesViewModel
import com.tickettrack.app.ui.transportista.TransportistaHomeViewModel
import com.tickettrack.app.ui.transportista.TransportistaTripsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    // ============ OKHTTP CLIENT ============
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ============ RETROFIT INSTANCES ============

    // Retrofit para Auth
    single(named("auth")) {
        Retrofit.Builder()
            .baseUrl("https://tickettrakedauth.runasp.net/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit para Drivers
    single(named("drivers")) {
        Retrofit.Builder()
            .baseUrl("https://microserviciostickettrack-production.up.railway.app/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit para Budget Requests
    single(named("budget")) {
        Retrofit.Builder()
            .baseUrl("https://tickettrack-budget-production.up.railway.app/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit para Expenses
    // Retrofit para Expenses
    single(named("expenses")) {
        Retrofit.Builder()
            .baseUrl("https://tickettrakedauth.runasp.net/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ============ API INTERFACES ============

    // AuthApi
    single {
        get<Retrofit>(named("auth")).create(AuthApi::class.java)
    }

    // DriversApi
    single {
        get<Retrofit>(named("drivers")).create(DriversApi::class.java)
    }

    // TripsApi (usa el mismo Retrofit de auth)
    single {
        get<Retrofit>(named("auth")).create(TripsApi::class.java)
    }

    // BudgetRequestsApi
    single {
        get<Retrofit>(named("budget")).create(BudgetRequestsApi::class.java)
    }

    // ExpensesApi
    single {
        get<Retrofit>(named("expenses")).create(ExpensesApi::class.java)
    }

    // ============ REPOSITORIES ============

    single<IAuthRepository> {
        AuthRepository(get())
    }

    single<IDriversRepository> {
        DriversRepository(get())
    }

    single<ITripsRepository> {
        TripsRepository(get())
    }

    single<IBudgetRequestsRepository> {
        BudgetRequestsRepository(get())
    }

    single<IExpensesRepository> {
        ExpensesRepository(get())
    }

    single { GoogleAuthViewModel(get()) }


    // ============ VIEW MODELS ============

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { DriversViewModel(get()) }
    viewModel { TripsViewModel(get()) }
    viewModel { TripDetailsViewModel(get(), get()) }
    viewModel { ExpensesViewModel(get(), get()) }
    viewModel { BudgetRequestDetailsViewModel(get()) }
    viewModel { ExpenseDetailsViewModel(get()) }

    // ViewModels - Transportista (USER)
    viewModel { TransportistaHomeViewModel(get(), get()) }
    viewModel { TransportistaTripsViewModel(get()) }
    viewModel { TransportistaExpensesViewModel(get(), get()) }
    viewModel { RegisterExpenseViewModel(get()) }

}