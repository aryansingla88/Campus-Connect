package com.example.campusconnect.feature.registrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationRepository
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationResponseRepository
import com.example.campusconnect.feature.registrations.data.repo.NetworkSubmissionRepository
import com.example.campusconnect.feature.registrations.data.repo.NetworkTeamRepository
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationResponseRepository
import com.example.campusconnect.feature.registrations.domain.repository.SubmissionRepository
import com.example.campusconnect.feature.registrations.domain.repository.TeamRepository
import com.example.campusconnect.feature.registrations.domain.usecase.GetRegistrationSetup
import com.example.campusconnect.feature.registrations.domain.usecase.GetRegistrationQuestions
import com.example.campusconnect.feature.registrations.domain.usecase.SaveQuickFields
import com.example.campusconnect.feature.registrations.domain.usecase.SaveRegistrationQuestions
import com.example.campusconnect.feature.registrations.domain.usecase.SaveRegistrationSettings
import com.example.campusconnect.feature.registrations.domain.usecase.RegistrationQuestionUseCases
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationConfigViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationFormViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import com.example.campusconnect.feature.registrations.viewmodel.SubmissionViewModel
import com.example.campusconnect.feature.registrations.viewmodel.ParticipantRegistrationViewModel
import com.example.campusconnect.feature.registrations.viewmodel.TeamPortalViewModel
import com.example.campusconnect.feature.registrations.viewmodel.OrganizerRegistrationWorkflowViewModel

/** Feature-scoped construction using the application's shared Retrofit client. */
object RegistrationDependencies {
    val api: RegistrationApi = RetrofitClient.registrationApi

    val registrationRepository: RegistrationRepository by lazy { NetworkRegistrationRepository(api) }
    val teamRepository: TeamRepository by lazy { NetworkTeamRepository(api, registrationRepository) }
    val submissionRepository: SubmissionRepository by lazy { NetworkSubmissionRepository(api, registrationRepository) }
    val responseRepository: RegistrationResponseRepository by lazy {
        NetworkRegistrationResponseRepository(api, registrationRepository)
    }

    val getRegistrationSetup by lazy { GetRegistrationSetup(registrationRepository) }
    val saveRegistrationSettings by lazy { SaveRegistrationSettings(registrationRepository) }
    val saveQuickFields by lazy { SaveQuickFields(registrationRepository) }

    val registrationConfigViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { RegistrationConfigViewModel(registrationRepository) }
    }
    val registrationFormViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { RegistrationFormViewModel(questionUseCases) }
    }
    val organizerRegistrationWorkflowViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { OrganizerRegistrationWorkflowViewModel() }
    }
    val questionUseCases by lazy {
        RegistrationQuestionUseCases(
            GetRegistrationQuestions(registrationRepository),
            SaveRegistrationQuestions(registrationRepository),
        )
    }
    val registrationResponsesViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { RegistrationResponsesViewModel(responseRepository) }
    }
    val submissionViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { SubmissionViewModel(submissionRepository) }
    }
    val userRegistrationViewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                @Suppress("UNCHECKED_CAST")
                return ParticipantRegistrationViewModel(registrationRepository, SavedStateHandle()) as VM
            }
        }
    }
    val teamPortalViewModelFactory: ViewModelProvider.Factory by lazy {
        factory { TeamPortalViewModel(teamRepository) }
    }

    private fun <T : ViewModel> factory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
        }
}
