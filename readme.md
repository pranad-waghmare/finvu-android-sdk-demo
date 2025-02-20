# Finvu Android Demo App

This demo application showcases the implementation and flows of the Finvu Android SDK. It demonstrates account discovery, linking, and consent management functionalities.

## Getting Started

1. Configure GitHub credentials in settings.gradle.kts
2. Build and run the application
3. Use test credentials to explore the flows
4. Follow the sequential flows from login to consent management

## Key Flows

### 1. Authentication Flow
- Initial login screen where user enters:
    - Username
    - Mobile number
    - Consent Handle ID
- OTP verification
- On successful verification, user is redirected to main dashboard

### 2. Main Dashboard Flow
- Displays list of linked accounts
- Provides options to:
    - Add new account
    - Process consent
- Fetches and displays all linked accounts in a recycler view

### 3. Account Discovery & Linking Flow
1. Popular Search
    - Displays list of available FIPs (Financial Information Providers)
    - User selects a FIP to proceed

2. Account Discovery
    - User enters mobile number (mandatory)
    - Optional PAN number input
    - Fetches available accounts from selected FIP
    - Shows both unlinked and already linked accounts
    - Allows selection of multiple accounts for linking

3. Account Linking Confirmation
    - OTP verification for selected accounts
    - On successful verification, accounts are linked
    - Redirects back to main dashboard

### 4. Consent Management Flow

#### Pre-defined Consent Handle IDs
For demonstration purposes, the app uses predefined consent handle IDs:

var consentHandleIds = mutableListOf(
"c518e2a3-d738-4241-a744-49e23832a643",
"62584175-37e4-462f-a4c9-3b95247b80c3",
"ef4c9d2d-90a5-410c-a212-86c10846cf13"
)


#### A. Consent Details Display
- Shows comprehensive consent information:
    - Purpose
    - Data fetch frequency
    - Data usage period
    - Date ranges
    - Account types requested

#### B. Account Selection
- Lists linked accounts
- Allows selection of accounts for consent

#### C. Consent Actions
1. Multi Consent Flow
    - Uses single consent handle ID (index 0) for all selected accounts
    - Processes all accounts in one API call
    - Simpler implementation for basic use cases
    - Example implementation:
    ```kotlin
    fun multiConsentFlow() {
        FinvuManager.shared.approveConsentRequest(
            consentDetailList[LoginActivity.consentHandleIds[0]]!!,
            linkedAccountsSelectableAdapter!!.getSelectedItems()
        ) { result ->
            // Handle success/failure
        }
    }
    ```

2. Split Consent Flow
    - Creates separate consent requests for each selected account
    - Uses different consent handle IDs for each account
    - First account treated as parent consent, others as child consents
    - Validates selected accounts match available consent handle IDs
    - Example implementation:
    ```kotlin
    fun splitConsentFlow() {
        if (linkedAccountsSelectableAdapter!!.getSelectedItems().size != LoginActivity.consentHandleIds.size) {
            // Shows error if mismatch
            return
        }
        
        for ((index, account) in linkedAccountsSelectableAdapter!!.getSelectedItems().withIndex()) {
            // Process each account with corresponding consent handle ID
        }
    }
    ```

3. Reject Consent
    - Denies the consent request using first consent handle ID
    - Cancels the entire consent process

#### Important Implementation Notes
##### Consent Handle ID Management:

    - Demo uses predefined IDs for simplicity
    - In production, generate new consent handle IDs for each selected account
    - Number of selected accounts must match available consent handle IDs

## Dependencies

The app uses the following Finvu SDK components:
- implementation("com.finvu.android:core-sdk:1.0.3") //Check the latest version
- implementation("com.finvu.android:client-sdk:1.0.3") //Check the latest version


## Production Considerations
1. Replace hardcoded consent handle IDs with dynamically generated ones
2. Implement proper validation for consent expiry and other parameters

Note: This is a demo application intended to showcase the Finvu Android SDK implementation. For production use, please refer to the official documentation and implement appropriate security measures.