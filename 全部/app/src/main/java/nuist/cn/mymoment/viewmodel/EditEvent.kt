package nuist.cn.mymoment.viewmodel

// A sealed class to represent one-time events from the ViewModel to the UI.
sealed class EditEvent {
    object NavigateBack : EditEvent()
}