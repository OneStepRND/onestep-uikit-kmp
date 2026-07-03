//
//  LocalizedStrings.swift
//  OneStepUIKit
//
//  Created by David Havkin on 25/03/2025.
//

import Foundation

struct LocalizedStrings {
    static var edit: String {
        NSLocalizedString("Edit", bundle: .module, value: "Edit", comment: "")
    }
    
    static var min: String {
        NSLocalizedString(" min", bundle: .module, value: " min", comment: "")
    }

    static var minCloseParen: String {
        NSLocalizedString(" min)", bundle: .module, value: " min)", comment: "")
    }

    static var capturedStepsButNotFullGaitAnalysis: String {
        NSLocalizedString("We managed to capture the number of steps but not a full gait analysis.", bundle: .module, value: "We managed to capture the number of steps but not a full gait analysis.", comment: "")
    }

    static var allowWhileUsingAppSingleQuotes: String {
        NSLocalizedString("\'Allow While Using App\'", bundle: .module, value: "\'Allow While Using App\'", comment: "")
    }
    
    //’Change to Always Allow’
    static var changeToAlwaysAllowSingleQuotes: String {
        NSLocalizedString("\'Change to Always Allow\'", bundle: .module, value: "\'Change to Always Allow\'", comment: "")
    }
    
    //When prompted, select
    static var whenPromptedSelect: String {
        NSLocalizedString("When prompted, select", bundle: .module, value: "When prompted, select", comment: "")
    }
    

    static var allowQuotes: String {
        NSLocalizedString("'Allow'", bundle: .module, value: "'Allow'", comment: "")
    }
    
    static var allow: String {
        NSLocalizedString("Allow", bundle: .module, value: "Allow", comment: "")
    }
    
    
    
    

    static var upToWithParen: String {
        NSLocalizedString("(Up to ", bundle: .module, value: "(Up to ", comment: "")
    }

    static var slash100: String {
        NSLocalizedString(" / 100", bundle: .module, value: " / 100", comment: "")
    }

    static var oneInsoles: String {
        NSLocalizedString("1 Insoles", bundle: .module, value: "1 Insoles", comment: "")
    }

    static var oneMinute: String {
        NSLocalizedString("1 minute", bundle: .module, value: "1 minute", comment: "")
    }

    static var threeMinutes: String {
        NSLocalizedString("3 minutes", bundle: .module, value: "3 minutes", comment: "")
    }

    static var fiveMinutes: String {
        NSLocalizedString("5 minutes", bundle: .module, value: "5 minutes", comment: "")
    }

    
    static var isHebrewVideo: Bool {
        NSLocalizedString("videoLanguage", bundle: .module, value: "en", comment: "Video language code") == "he"
    }

    /// Whether the recording flow is currently presented in Hebrew.
    /// Used to disable features that are intentionally excluded for Hebrew
    /// (e.g. the timed walk voice-over cues in 2MWT / 6MWT).
    static var isHebrew: Bool {
        textToSpeechVoiceLanguage.hasPrefix("he")
    }

    /// TTS voice language code for the current locale
    /// RTL NOTE: Ensure Localizable.xcstrings has Hebrew translation: "he-IL" for Hebrew TTS
    /// Falls back to locale detection if not localized
    static var textToSpeechVoiceLanguage: String {
        let localizedValue = NSLocalizedString("textToSpeechVoiceLanguage", bundle: .module, value: "en-US", comment: "TTS voice language code")

        // If the localized value is the same as default, detect from current locale
        if localizedValue == "en-US" {
            if let languageCode = Locale.current.language.languageCode?.identifier {
                switch languageCode {
                case "he": return "he-IL"  // Hebrew
                case "ar": return "ar-SA"  // Arabic
                case "ru": return "ru-RU"  // Russian
                case "ro": return "ro-RO"  // Romanian
                case "uk": return "uk-UA"  // Ukrainian
                default: return "en-US"
                }
            }
        }

        return localizedValue
    }

    static var abnormalResults30To50: String {
        NSLocalizedString("30-50: Abnormal results", bundle: .module, value: "30-50: Abnormal results", comment: "")
    }
    
    static var ok: String {
        NSLocalizedString("OK", bundle: .module, value: "OK", comment: "")
    }

    static var outsideNormalRange50To65: String {
        NSLocalizedString("50-65: Outside normal range", bundle: .module, value: "50-65: Outside normal range", comment: "")
    }

    static var withinNormalRange65To100: String {
        NSLocalizedString("65-100: Within normal range", bundle: .module, value: "65-100: Within normal range", comment: "")
    }

    static var addNoteAboutMeasurement: String {
        NSLocalizedString("Add a note about the measurement", bundle: .module, value: "Add a note about the measurement", comment: "")
    }

    static var againstTheThigh: String {
        NSLocalizedString("Against the thigh", bundle: .module, value: "Against the thigh", comment: "")
    }

    static var analysisInProgress: String {
        NSLocalizedString("Analysis in progress", bundle: .module, value: "Analysis in progress", comment: "")
    }

    static var analyzing: String {
        NSLocalizedString("Analyzing", bundle: .module, value: "Analyzing", comment: "")
    }

    static var analyzingInProgress: String {
        NSLocalizedString("Analyzing in progress", bundle: .module, value: "Analyzing in progress", comment: "")
    }

    static var assistiveDevice: String {
        NSLocalizedString("Assistive device:", bundle: .module, value: "Assistive device:", comment: "")
    }
    
    static var levelOfAssistanceIndependent: String {
        NSLocalizedString("Independent", bundle: .module, value: "Independent", comment: "")
    }
    
    static var levelOfAssistanceModifiedIndependent: String {
        NSLocalizedString("Modified independent", bundle: .module, value: "Modified independent", comment: "")
    }
    
    static var levelOfAssistanceStandBy: String {
        NSLocalizedString("Standby assistance", bundle: .module, value: "Standby assistance", comment: "")
    }
    
    static var levelOfAssistanceMinimal: String {
        NSLocalizedString("Minimal assistance", bundle: .module, value: "Minimal assistance", comment: "")
    }
    
    static var levelOfAssistanceModerate: String {
        NSLocalizedString("Moderate assistance", bundle: .module, value: "Moderate assistance", comment: "")
    }
    
    static var levelOfAssistanceMaximum: String {
        NSLocalizedString("Maximum assistance", bundle: .module, value: "Maximum assistance", comment: "")
    }
    
    static var levelOfAssistanceTotal: String {
        NSLocalizedString("Total assistance", bundle: .module, value: "Total assistance", comment: "")
    }
    
    static var levelOfAssistance: String {
        NSLocalizedString("Level of assistance:", bundle: .module, value: "Level of assistance:", comment: "")
    }
    
    static var unableToPerformAtThisTime: String {
        NSLocalizedString("Unable to complete at this time", bundle: .module, value: "Unable to complete at this time", comment: "")
    }

    static var background: String {
        NSLocalizedString("Background", bundle: .module, value: "Background", comment: "")
    }
    
    static var info: String {
        NSLocalizedString("Info", bundle: .module, value: "Info", comment: "")
    }
    
    static var measurementBreakdownInfoText: String {
        NSLocalizedString("The sum of the measurement breakdown may exceed the measurement duration due to overlap between the different parts.", bundle: .module, value: "The sum of the measurement breakdown may exceed the measurement duration due to overlap between the different parts.", comment: "")
    }

    static var backgroundRecordingsInfo: String {
        NSLocalizedString("Background recordings are measurements taken automatically, without actively accessing the app, and only after permissions are approved.", bundle: .module, value: "Background recordings are measurements taken automatically, without actively accessing the app, and only after permissions are approved.", comment: "")
    }

    static var barefoot: String {
        NSLocalizedString("Barefoot", bundle: .module, value: "Barefoot", comment: "")
    }

    static var brace: String {
        NSLocalizedString("Brace", bundle: .module, value: "Brace", comment: "")
    }
    
    // Choose location
    static var selectLocation: String {
        NSLocalizedString("Select Location", bundle: .module, value: "Select Location", comment: "")
    }

    static var chooseHowLongToWalkToday: String {
        NSLocalizedString("Choose how long you want to walk today", bundle: .module, value: "Choose how long you want to walk today", comment: "")
    }

    static var chooseWherePhoneWillBePlaced: String {
        NSLocalizedString("Choose where the phone will be placed", bundle: .module, value: "Choose where the phone will be placed", comment: "")
    }

    static var connectionIssue: String {
        NSLocalizedString("Connection issue", bundle: .module, value: "Connection issue", comment: "")
    }

    static var continueText: String {
        NSLocalizedString("Continue", bundle: .module, value: "Continue", comment: "")
    }
    
    //Change to ‘while using’
    static var tapWhileUsing: String {
        NSLocalizedString("Tap While Using the App", bundle: .module, value: "Tap While Using the App", comment: "")
    }
    
    //Change to ’Always’
    static var tapAlways: String {
        NSLocalizedString("Tap Always", bundle: .module, value: "Tap Always", comment: "")
    }
    
    //How is my data used?
    static var dataUsage: String {
        NSLocalizedString("How is my data used?", bundle: .module, value: "How is my data used?", comment: "")
    }
    
    //Please allow access to your Motion & Fitness.
    
    //Go to your device settings
    static var goToDeviceSettings: String {
        NSLocalizedString("Go to your device settings", bundle: .module, value: "Go to your device settings", comment: "")
    }
    
    //Go to your device settings
    static var goToDeviceSettingsAndThen: String {
        NSLocalizedString("Go to your device settings and then", bundle: .module, value: "Go to your device settings and then", comment: "")
    }
    
    //Go to your device settings and then toggle on
    static var goToDeviceSettingsAndThenToggleOn: String {
        NSLocalizedString("Go to your device settings\n and then toggle on", bundle: .module, value: "Go to your device settings\n and then toggle on", comment: "")
    }
    
    //Choose
    static var choose: String {
        NSLocalizedString("Choose", bundle: .module, value: "Choose", comment: "")
    }
    
    //Tap
    static var tap: String {
        NSLocalizedString("Tap", bundle: .module, value: "Tap", comment: "")
    }
    
    //Find the ‘Health’ app either in settings or apps
    static var findHealthApp: String {
        NSLocalizedString("Find the ‘Health’ app either in settings or apps", bundle: .module, value: "Find the ‘Health’ app either in settings or apps", comment: "")
    }
    
    //Go to ‘Data Access & Devices’
    static var goToDataAccessDevices: String {
        NSLocalizedString("Go to ‘Data Access & Devices’", bundle: .module, value: "Go to ‘Data Access & Devices’", comment: "")
    }
    
    //Toggle on Motion and Fitness
    static var motionAndFitness: String {
        NSLocalizedString("Motion and Fitness", bundle: .module, value: "Motion and Fitness", comment: "")
    }
    
    static var dataUsageDescriptionMF: String {
        NSLocalizedString("We access your phone’s motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.", bundle: .module, value: "We access your phone’s motion sensors to analyze steps, balance, and how you walk. This helps us provide detailed, clinically relevant insights.", comment: "")
    }
    
    //We use motion-related location data during walks and other standard movement measurements. This helps us analyze your movement, but we never track or store your exact location.
    static var dataUsageDescriptionLocation: String {
        NSLocalizedString("We use motion-related location data during walks and other standard movement measurements. This helps us analyze your movement, but we never track or store your exact location.", bundle: .module, value: "We use motion-related location data during walks and other standard movement measurements. This helps us analyze your movement, but we never track or store your exact location.", comment: "")
    }
    
    //We access walking and activity data from HealthKit, including step count, walking speed, and other movement details, to help assess your mobility overall activity levels.
    static var dataUsageDescriptionHealthKit: String {
        NSLocalizedString("We access walking and activity data from HealthKit, including step count, walking speed, and other movement details, to help assess your mobility overall activity levels.", bundle: .module, value: "We access walking and activity data from HealthKit, including step count, walking speed, and other movement details, to help assess your mobility overall activity levels.", comment: "")
    }

    static var date: String {
        NSLocalizedString("Date", bundle: .module, value: "Date", comment: "")
    }

    static var deletionFailed: String {
        NSLocalizedString("Deletion failed", bundle: .module, value: "Deletion failed", comment: "")
    }

    static var discard: String {
        NSLocalizedString("Discard", bundle: .module, value: "Discard", comment: "")
    }

    static var discardMeasurement: String {
        NSLocalizedString("Discard measurement", bundle: .module, value: "Discard measurement", comment: "")
    }

    static var done: String {
        NSLocalizedString("Done", bundle: .module, value: "Done", comment: "")
    }

    static var duration: String {
        NSLocalizedString("Duration:", bundle: .module, value: "Duration:", comment: "")
    }

    static var examplePerformedTestOnCarpet: String {
        NSLocalizedString("E.g. performed test on a carpet.", bundle: .module, value: "E.g. performed test on a carpet.", comment: "")
    }

    static var trackYourStepCount: String {
        NSLocalizedString("Track your step count", bundle: .module, value: "Track your step count", comment: "")
    }
    
    //Please allow Health Kit access:
    static var healthKitAccessDescription: String {
        NSLocalizedString("Please allow Health Kit access:", bundle: .module, value: "Please allow Health Kit access:", comment: "")
    }
    
    //‘Turn On All’
    static var turnOnAll: String {
        NSLocalizedString("‘Turn On All’", bundle: .module, value: "‘Turn On All’", comment: "")
    }
    
    //‘Allow’.
    static var allowHealthKit: String {
        NSLocalizedString("‘Allow’", bundle: .module, value: "‘Allow’", comment: "")
    }

    static var makeSurePhoneIs: String {
        NSLocalizedString("Make sure your phone is in a front pants pocket or against your thigh. ", bundle: .module, value: "Make sure your phone is in a front pants pocket or against your thigh. ", comment: "")
    }
    
    static var tapStart: String {
        NSLocalizedString("Tap “Start” to begin the test.", bundle: .module, value: "Tap “Start” to begin the test.", comment: "")
    }
    
    static var placePhoneSecurely: String {
        NSLocalizedString("Place your phone securely in your pocket or flat against your thigh.", bundle: .module, value: "Place your phone securely in your pocket or flat against your thigh.", comment: "")
    }
    
    static var walkAtSteadyPace: String {
        NSLocalizedString("Walk at a steady pace back and forth along a flat path (like a hallway).", bundle: .module, value: "Walk at a steady pace back and forth along a flat path (like a hallway).", comment: "")
    }
    
    static var turnAroundAtEndOfHallway: String {
        NSLocalizedString("Turn around at each end of the hallway or marked walking area.", bundle: .module, value: "Turn around at each end of the hallway or marked walking area.", comment: "")
    }
    
    static var keepThePhoneInPlace: String {
        NSLocalizedString("Keep the phone in place throughout, do not remove or adjust it.", bundle: .module, value: "Keep the phone in place throughout, do not remove or adjust it.", comment: "")
    }
    
    static var theAppWillLetYouKnow6Min: String {
        NSLocalizedString("The app will let you know when 6 minutes are over — no need to track time.", bundle: .module, value: "The app will let you know when 6 minutes are over — no need to track time.", comment: "")
    }

    static var theAppWillLetYouKnow2Min: String {
        NSLocalizedString("The app will let you know when 2 minutes are over — no need to track time.", bundle: .module, value: "The app will let you know when 2 minutes are over — no need to track time.", comment: "")
    }

    static var error: String {
        NSLocalizedString("Error", bundle: .module, value: "Error", comment: "")
    }

    static var footwear: String {
        NSLocalizedString("Footwear:", bundle: .module, value: "Footwear:", comment: "")
    }
    
    static var useOfHands: String {
        NSLocalizedString("Use of hands", bundle: .module, value: "Did you use hands for support?", comment: "")
    }
    
    static var yesUsedHands: String {
        NSLocalizedString("yesUsedHands", bundle: .module, value: "Yes", comment: "")
    }
    
    static var noUsedHands: String {
        NSLocalizedString("noUsedHands", bundle: .module, value: "No", comment: "")
    }

    static var successfulAnalysisStandUp: String {
        NSLocalizedString("For a successful analysis you need to stand up and sit back down as many times as you can until the time counter stops.", bundle: .module, value: "For a successful analysis you need to stand up and sit back down as many times as you can until the time counter stops.", comment: "")
    }

    static var successfulAnalysisWalkStraight: String {
        NSLocalizedString("For a successful analysis you need to walk in a straight line.", bundle: .module, value: "For a successful analysis you need to walk in a straight line.", comment: "")
    }

    static var gaitLab: String {
        NSLocalizedString("Gait data", bundle: .module, value: "Gait data", comment: "")
    }

    static var generatingReport: String {
        NSLocalizedString("Generating report", bundle: .module, value: "Generating report", comment: "")
    }

    static var getReady: String {
        NSLocalizedString("Get ready", bundle: .module, value: "Get ready", comment: "")
    }

    static var go: String {
        NSLocalizedString("Go", bundle: .module, value: "Go", comment: "")
    }

    static var goToSettings: String {
        NSLocalizedString("Go to settings", bundle: .module, value: "Go to settings", comment: "")
    }

    static var greatJobGettingUpAndMeasuring: String {
        NSLocalizedString("Great job getting up and measuring! Keep  the phone snug in your pants pocket or hold it against the thigh and follow the test instructions.", bundle: .module, value: "Great job getting up and measuring! Keep  the phone snug in your pants pocket or hold it against the thigh and follow the test instructions.", comment: "")
    }

    static var greatJobGettingUpAndWalkingForComplete: String {
        NSLocalizedString("Great job getting up and walking!\nFor a complete gait analysis keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", bundle: .module, value: "Great job getting up and walking!\nFor a complete gait analysis keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", comment: "")
    }
    
    static var greatJobGettingUpAndWalking: String {
        NSLocalizedString("Great job getting up and walking! Keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", bundle: .module, value: "Great job getting up and walking! Keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", comment: "")
    }

    static var highlights: String {
        NSLocalizedString("Highlights", bundle: .module, value: "Highlights", comment: "")
    }

    static var hints: String {
        NSLocalizedString("Hints:", bundle: .module, value: "Hints:", comment: "")
    }

    static var inApp: String {
        NSLocalizedString("In app", bundle: .module, value: "In app", comment: "")
    }

    static var inThePocket: String {
        NSLocalizedString("In the pocket", bundle: .module, value: "In the pocket", comment: "")
    }
    
    static var inPosition: String {
        NSLocalizedString("In position", bundle: .module, value: "In position", comment: "")
    }

    static var instructions: String {
        NSLocalizedString("Instructions:", bundle: .module, value: "Instructions:", comment: "")
    }

    static var invalidQuantityType: String {
        NSLocalizedString("Invalid quantity type.", bundle: .module, value: "Invalid quantity type.", comment: "")
    }

    static var internetConnectionIssues: String {
        NSLocalizedString("It seems you have internet connection issues. Check your internet connection and press reload. The measurement will be uploaded and analyzed once we get internet connection.", bundle: .module, value: "It seems you have internet connection issues. Check your internet connection and press reload. The measurement will be uploaded and analyzed once we get internet connection.", comment: "")
    }

    static var longWalk: String {
        NSLocalizedString("Long walk", bundle: .module, value: "Long walk", comment: "")
    }

    static var resultsBeingProcessed: String {
        NSLocalizedString("Your results are being processed. You can continue using the app, and your analysis will appear in your History tab shortly.", bundle: .module, value: "Your results are being processed. You can continue using the app, and your analysis will appear in your History tab shortly.", comment: "")
    }

    static var increasePhoneVolume: String {
        NSLocalizedString("Make sure to increase your phone's volume so you can hear the instructions", bundle: .module, value: "Make sure to increase your phone's volume so you can hear the instructions", comment: "")
    }

    static var minOnly: String {
        NSLocalizedString("min", bundle: .module, value: "min", comment: "")
    }

    static var na: String {
        NSLocalizedString("N/A", bundle: .module, value: "N/A", comment: "")
    }

    static var noBackgroundData: String {
        NSLocalizedString("No background data collected", bundle: .module, value: "No background data collected", comment: "")
    }

    static var noInAppData: String {
        NSLocalizedString("No in app data collected", bundle: .module, value: "No in app data collected", comment: "")
    }

    static var nonSkidSocks: String {
        NSLocalizedString("Non-skid socks", bundle: .module, value: "Non-skid socks", comment: "")
    }

    static var none: String {
        NSLocalizedString("None", bundle: .module, value: "None", comment: "")
    }

    static var notes: String {
        NSLocalizedString("Notes:", bundle: .module, value: "Notes:", comment: "")
    }

    static var of100: String {
        NSLocalizedString("of 100", bundle: .module, value: "of 100", comment: "")
    }

    static var walkRecordedView: String {
        NSLocalizedString("Once a walk is recorded, you can view it here.", bundle: .module, value: "Once a walk is recorded, you can view it here.", comment: "")
    }

    static var oops: String {
        NSLocalizedString("Oops…", bundle: .module, value: "Oops…", comment: "")
    }

    static var placePhoneAgainstThigh: String {
        NSLocalizedString("Place the phone against the thigh", bundle: .module, value: "Place the phone against the thigh", comment: "")
    }

    static var placePhoneInPocket: String {
        NSLocalizedString("Place the phone in the pocket", bundle: .module, value: "Place the phone in the pocket", comment: "")
    }
    
    static var placePhoneInPosition: String {
        NSLocalizedString("Place the phone in position", bundle: .module, value: "Place the phone in position", comment: "")
    }

    static var actionCannotBeUndone: String {
        NSLocalizedString("Please note - this action cannot be undone.\nAny previous reports generated based on this data will not be altered retroactively.", bundle: .module, value: "Please note - this action cannot be undone.\nAny previous reports generated based on this data will not be altered retroactively.", comment: "")
    }

    static var pleaseTryAgainLater: String {
        NSLocalizedString("Please try again later", bundle: .module, value: "Please try again later", comment: "")
    }

    static var preparingResults: String {
        NSLocalizedString("Preparing results", bundle: .module, value: "Preparing results", comment: "")
    }

    static var findClearStraightPath: String {
        NSLocalizedString("Find a clear, straight path to walk.", bundle: .module, value: "Find a clear, straight path to walk.", comment: "")
    }

    static var recordAWalk: String {
        NSLocalizedString("Record a walk", bundle: .module, value: "Record a walk", comment: "")
    }

    static var recordingInProgress: String {
        NSLocalizedString("Recording in progress", bundle: .module, value: "Recording in progress", comment: "")
    }

    // MARK: - Walk voice-over cues (6MWT / 2MWT) — OS-15738. English only (Hebrew not required).
    static var walkCue5MinutesToGo: String {
        NSLocalizedString("You have 5 minutes to go.", bundle: .module, value: "You have 5 minutes to go.", comment: "Voice-over cue spoken during 6MWT recording")
    }

    static var walkCue4MinutesToGo: String {
        NSLocalizedString("You have 4 minutes to go.", bundle: .module, value: "You have 4 minutes to go.", comment: "Voice-over cue spoken during 6MWT recording")
    }

    static var walkCueHalfwayDone: String {
        NSLocalizedString("You are halfway done.", bundle: .module, value: "You are halfway done.", comment: "Voice-over cue spoken during 6MWT recording")
    }

    static var walkCue2MinutesLeft: String {
        NSLocalizedString("You have only 2 minutes left.", bundle: .module, value: "You have only 2 minutes left.", comment: "Voice-over cue spoken during 6MWT recording")
    }

    static var walkCue1MinuteToGo: String {
        NSLocalizedString("You only have 1 minute to go.", bundle: .module, value: "You only have 1 minute to go.", comment: "Voice-over cue spoken during 6MWT/2MWT recording")
    }

    static var walkCue15SecondsToGo: String {
        NSLocalizedString("You have 15 seconds to go.", bundle: .module, value: "You have 15 seconds to go.", comment: "Voice-over cue spoken during 6MWT/2MWT recording")
    }

    static var reload: String {
        NSLocalizedString("Reload", bundle: .module, value: "Reload", comment: "")
    }

    static var reviewFollowingTags: String {
        NSLocalizedString("Review the following tags", bundle: .module, value: "Review the following tags", comment: "")
    }

    static var score: String {
        NSLocalizedString("Score", bundle: .module, value: "Score", comment: "")
    }

    static var scoreUnavailable: String {
        NSLocalizedString("Score unavailable", bundle: .module, value: "Score unavailable", comment: "")
    }

    static var scoreTextWalk: String {
        NSLocalizedString("Score: ", bundle: .module, value: "Score: ", comment: "")
    }
    
    static func repetitionsText(_ count: Int) -> String {
        let rawRepsWithCount = String.localizedStringWithFormat(
            NSLocalizedString("reps", bundle: .module, value: "reps", comment: ""), count)
        let repsWithoutNumber = rawRepsWithCount.split(separator: " ").dropFirst().joined()
        
        return repsWithoutNumber
    }
    
    static var sec: String {
        NSLocalizedString("sec", bundle: .module, value: "sec", comment: "")
    }
    
    static var degrees: String {
        NSLocalizedString("degrees", bundle: .module, value: "degrees", comment: "")
    }
    
    static var scoreTextSTS: String {
        NSLocalizedString("Number of reps: ", bundle: .module, value: "Number of reps: ", comment: "")
    }
    
    static var scoreTextTUG: String {
        NSLocalizedString("Duration: ", bundle: .module, value: "Duration: ", comment: "")
    }
    
    static var scoreTextROM: String {
        NSLocalizedString("Angle: ", bundle: .module, value: "Angle: ", comment: "")
    }
    
    static var scoreTextSixMinWalk: String {
        NSLocalizedString("Distance: ", bundle: .module, value: "Distance: ", comment: "")
    }

    static var shoeAdjustment: String {
        NSLocalizedString("Shoe adjustment", bundle: .module, value: "Shoe adjustment", comment: "")
    }

    static var slideToStop: String {
        NSLocalizedString("Slide to stop", bundle: .module, value: "Slide to stop", comment: "")
    }

    static var slippers: String {
        NSLocalizedString("Slippers", bundle: .module, value: "Slippers", comment: "")
    }

    static var smoBrace: String {
        NSLocalizedString("SMO brace", bundle: .module, value: "SMO brace", comment: "")
    }

    static var start: String {
        NSLocalizedString("Start", bundle: .module, value: "Start", comment: "")
    }

    static var startNow: String {
        NSLocalizedString("Start now", bundle: .module, value: "Start now", comment: "")
    }

    static var status: String {
        NSLocalizedString("Status:", bundle: .module, value: "Status:", comment: "")
    }

    static var steps: String {
        NSLocalizedString("steps", bundle: .module, value: "steps", comment: "")
    }

    static var tags: String {
        NSLocalizedString("Tags:", bundle: .module, value: "Tags:", comment: "")
    }

    static var pleaseAllow: String {
        NSLocalizedString("Please allow", bundle: .module, value: "Please allow", comment: "")
    }
    
    static var toAnalyzeMovement: String {
        NSLocalizedString("to analyze your movement and measure your progress.", bundle: .module, value: "to analyze your movement and measure your progress.", comment: "")
    }
    
    static var yourDataIsSecurelyStored: String {
        NSLocalizedString("Your data is securely stored and shared only with your healthcare provider.", bundle: .module, value: "Your data is securely stored and shared only with your healthcare provider.", comment: "")
    }

    static var problemConnectingToServerTryAgainLater: String {
        NSLocalizedString("There was a problem connecting to the server - please try again later.", bundle: .module, value: "There was a problem connecting to the server - please try again later.", comment: "")
    }

    static var measurementRecordingTooShort: String {
        NSLocalizedString("This measurement's recording was too short", bundle: .module, value: "This measurement's recording was too short", comment: "")
    }

    static var analyzingYourResults: String {
        NSLocalizedString("Analyzing Your Results", bundle: .module, value: "Analyzing Your Results", comment: "")
    }

    static var permYourDataIsSafeWithUs: String {
        NSLocalizedString("Your data is safe with us", bundle: .module, value: "Your data is safe with us", comment: "")
    }

    static var tryAgain: String {
        NSLocalizedString("Try again", bundle: .module, value: "Try again", comment: "")
    }

    static var ucblBrace: String {
        NSLocalizedString("UCBL brace", bundle: .module, value: "UCBL brace", comment: "")
    }

    static var unableToComputeDateFor7DaysAgo: String {
        NSLocalizedString("Unable to compute date for 7 days ago.", bundle: .module, value: "Unable to compute date for 7 days ago.", comment: "")
    }

    static var viewInstructions: String {
        NSLocalizedString("View instructions", bundle: .module, value: "View instructions", comment: "")
    }

    static var walk: String {
        NSLocalizedString("Walk", bundle: .module, value: "Walk", comment: "")
    }
    
    static var sixMinWalk: String {
        NSLocalizedString("Six Minute Walk Test", bundle: .module, value: "Six Minute Walk Test", comment: "")
    }

    static var twoMinWalk: String {
        NSLocalizedString("Two Minute Walk Test", bundle: .module, value: "Two Minute Walk Test", comment: "")
    }

    static var stairs: String {
        NSLocalizedString("Stairs", bundle: .module, value: "Stairs", comment: "")
    }

    static var asFarAsYouCan: String {
        NSLocalizedString("If your path is short, walk as far as you can, then turn around and repeat until time is up.", bundle: .module, value: "If your path is short, walk as far as you can, then turn around and repeat until time is up.", comment: "")
    }

    static var walkScoreDailyAverage: String {
        NSLocalizedString("Walk score daily average", bundle: .module, value: "Walk score daily average", comment: "")
    }

    static var walker: String {
        NSLocalizedString("Walker", bundle: .module, value: "Walker", comment: "")
    }

    static var couldntCaptureSteps: String {
        NSLocalizedString("We couldn't capture your steps", bundle: .module, value: "We couldn't capture your steps", comment: "")
    }

    static var couldntDetectMovement: String {
        NSLocalizedString("We couldn't detect any movement", bundle: .module, value: "We couldn't detect any movement", comment: "")
    }

    static var walkInComfyPace: String {
        NSLocalizedString("Walk at a comfortable pace.", bundle: .module, value: "Walk at a comfortable pace.", comment: "")
    }
    
    static var stopIfUnComfy: String {
        NSLocalizedString("Stop if you feel uncomfortable.", bundle: .module, value: "Stop if you feel uncomfortable.", comment: "")
    }

    static var whatAssistiveDeviceDidYouUse: String {
        NSLocalizedString("What assistive device did you use?", bundle: .module, value: "What assistive device did you use?", comment: "")
    }
    
    static var whatIsThePatientsLevelOfAssistance: String {
        NSLocalizedString("What is the patient’s level of assistance?", bundle: .module, value: "What is the patient’s level of assistance?", comment: "")
    }

    static var whichTypeOfFootwear: String {
        NSLocalizedString("Which type of footwear was worn during the test?", bundle: .module, value: "Which type of footwear was worn during the test?", comment: "")
    }

    static var preRecordingAssistiveDeviceTitle: String {
        NSLocalizedString("Choose your assistive device", bundle: .module, value: "Choose your assistive device", comment: "")
    }

    static var preRecordingFootwearTitle: String {
        NSLocalizedString("Choose your footwear", bundle: .module, value: "Choose your footwear", comment: "")
    }
    
    static var seconds: String {
        NSLocalizedString("seconds", bundle: .module, value: "seconds", comment: "")
    }

    static var withShoes: String {
        NSLocalizedString("With shoes", bundle: .module, value: "With shoes", comment: "")
    }

    
    static var motionAndFitnessActivityAccessRequired: String {
        NSLocalizedString("In order to monitor your measurements, access to your motion and fitness activity is required.", bundle: .module, value: "In order to monitor your measurements, access to your motion and fitness activity is required.", comment: "")
    }
    
    //In order to monitor your measurements, access to your location is required
    static var locationAccessRequired: String {
        NSLocalizedString("In order to monitor your measurements, access to your location is required", bundle: .module, value: "In order to monitor your measurements, access to your location is required", comment: "")
    }
    
    //Please allow access to your Motion & Fitness data so we can provide the most accurate assessments.
    static var pleaseAlloMotionAndFitnessPermissionsDescription: String {
        NSLocalizedString("Please allow access to your Motion & Fitness data so we can provide the most accurate assessments.", bundle: .module, value: "Please allow access to your Motion & Fitness data so we can provide the most accurate assessments.", comment: "")
    }
    
    //Location access is currently limited
    static var locationAccessLimited: String {
        NSLocalizedString("Location access is currently limited", bundle: .module, value: "Location access is currently limited", comment: "")
    }
    
    //Please allow access to your location while the app in open. When prompted tap
    static var locationPermissionsDescription: String {
        NSLocalizedString("Please allow access to your location while the app is open. When prompted tap ", bundle: .module, value: "Please allow access to your location while the app is open. When prompted tap ", comment: "")
    }
    
    //Please allow access to your location while the app in closed.
    static var locationPermissionsDescriptionBackground: String {
        NSLocalizedString("Please allow access to your location even when the app is closed.", bundle: .module, value: "Please allow access to your location even when the app is closed.", comment: "")
    }
    
    //Please allow access to your location while the app is in use..
    static var locationPermissionsDescriptionForeground: String {
        NSLocalizedString("Please allow access to your location while the app is in use.", bundle: .module, value: "Please allow access to your location while the app is in use.", comment: "")
    }
    
    //Turn location access back on to analyze your walks, even when the app is closed, for the most accurate, real-life insights.
    static var locationPermissionsDescriptionTurnOn: String {
        NSLocalizedString("Turn location access back on to analyze your walks, even when the app is closed, for the most accurate, real-life insights.", bundle: .module, value: "Turn location access back on to analyze your walks, even when the app is closed, for the most accurate, real-life insights.", comment: "")
    }
        
    //Get deeper insights into your movement
    static var getDeeperInsights: String {
        NSLocalizedString("Get deeper insights into your movement", bundle: .module, value: "Get deeper insights into your movement", comment: "")
    }
    
    //Get better assessments with real-life walks
    static var getBetterAssessments: String {
        NSLocalizedString("Get better assessments with real-life walks", bundle: .module, value: "Get better assessments with real-life walks", comment: "")
    }

    static var walkHadTooManyTurns: String {
        NSLocalizedString("Your walk had too many turns", bundle: .module, value: "Your walk had too many turns", comment: "")
    }
    
    static var greatJobForMeasuring: String {
        NSLocalizedString("Great job for measuring! Make sure to follow the measurement instructions to get an analysis.", bundle: .module, value: "Great job for measuring! Make sure to follow the measurement instructions to get an analysis.", comment: "")
    }
    
    static var successfulAnalysisRequired30Seconds: String {
        NSLocalizedString("For a successful analysis, you're required to measure 30 seconds.", bundle: .module, value: "For a successful analysis, you're required to measure 30 seconds.", comment: "")
    }
    
    static var didntDetectRepetitions: String {
        NSLocalizedString("We didn't detect repetitions", bundle: .module, value: "We didn't detect repetitions", comment: "")
    }
    
    
    static var couldNotDetectAnyMovement: String {
        NSLocalizedString("We could not detect any movement", bundle: .module, value: "We could not detect any movement", comment: "")
    }

    static var manuallyReported: String {
        NSLocalizedString("Manually reported", bundle: .module, value: "Manually reported", comment: "Pill shown on a measurement summary when the value was entered by the clinician rather than computed.")
    }

    static var documentResultTitle: String {
        NSLocalizedString("How many repetitions were performed?", bundle: .module, value: "How many repetitions were performed?", comment: "")
    }

    static var save: String {
        NSLocalizedString("Save", bundle: .module, value: "Save", comment: "")
    }

    static var enablePermissions: String {
        NSLocalizedString("Enable Permissions", bundle: .module, value: "Enable Permissions", comment: "")
    }
    
    static var youProviderCannotMonitor: String {
        NSLocalizedString("Your provider cannot monitor your measurements before you allow access to your location.", bundle: .module, value: "Your provider cannot monitor your measurements before you allow access to your location.", comment: "")
    }

    static var backgroundMonitoringOff: String {
        NSLocalizedString("Your background monitoring is currently off.", bundle: .module, value: "Your background monitoring is currently off.", comment: "")
    }

    static var enableBackgroundMonitoring: String {
        NSLocalizedString("Enable Background Monitoring", bundle: .module, value: "Enable Background Monitoring", comment: "")
    }

    static var sitToStand: String {
        NSLocalizedString("Sit to Stand", bundle: .module, value: "Sit to Stand", comment: "")
    }
    
    static var kneeExtension: String {
        NSLocalizedString("Knee Extension", bundle: .module, value: "Knee Extension", comment: "")
    }
    
    static var kneeFlexion: String {
        NSLocalizedString("Knee Flexion", bundle: .module, value: "Knee Flexion", comment: "")
    }

    static var sitWithBack: String {
        NSLocalizedString("Sit in a chair with your back against the back of the chair.", bundle: .module, value: "Sit in a chair with your back against the back of the chair.", comment: "")
    }
    
    static var sitWithBothFeet: String {
        NSLocalizedString("Sit on the chair with both feet on the floor.", bundle: .module, value: "Sit on the chair with both feet on the floor.", comment: "")
    }
    
    static var sitOnAFirmChair: String {
        NSLocalizedString("Sit on a firm chair.", bundle: .module, value: "Sit on a firm chair.", comment: "")
    }
    
    static var moveToTheEdge: String {
        NSLocalizedString("Move to the edge of the chair.", bundle: .module, value: "Move to the edge of the chair.", comment: "")
    }
    
    static var straightenLeg: String {
        NSLocalizedString("Straighten the measured leg in front of you as much as possible.", bundle: .module, value: "Straighten the measured leg in front of you as much as possible.", comment: "")
    }
    
    static var keepHeelOnTheGround: String {
        NSLocalizedString("Keep your heel resting on the ground, toes pointing up.", bundle: .module, value: "Keep your heel resting on the ground, toes pointing up.", comment: "")
    }
    
    static var placePhoneOnTheFrontOfMidThigh: String {
        NSLocalizedString("Place the phone on the front of your mid-thigh, then move it to your shin (just below the knee). Repeat this sequence 10 times.", bundle: .module, value: "Place the phone on the front of your mid-thigh, then move it to your shin (just below the knee). Repeat this sequence 10 times.", comment: "")
    }
    
    static var standUp: String {
        NSLocalizedString("Stand up.", bundle: .module, value: "Stand up.", comment: "")
    }
    
    static var crossArms: String {
        NSLocalizedString("Cross your arms against your chest (in an x shape).", bundle: .module, value: "Cross your arms against your chest (in an x shape).", comment: "")
    }

    static var sitStandAsManyTimes: String {
        NSLocalizedString("Stand up and sit down as many times as you can in 30 seconds.", bundle: .module, value: "Stand up and sit down as many times as you can in 30 seconds.", comment: "")
    }

    static var goToMarkerAndAround: String {
        NSLocalizedString("Walk 3 meters towards the marker and go around it.", bundle: .module, value: "Walk 3 meters towards the marker and go around it.", comment: "")
    }

    static var walkBackAndSit: String {
        NSLocalizedString("Walk back towards the chair and sit down.", bundle: .module, value: "Walk back towards the chair and sit down.", comment: "")
    }
    
    static var waitThreeSecondsWhileSittingDown: String {
        NSLocalizedString("Wait 3 seconds while sitting down.", bundle: .module, value: "Wait 3 seconds while sitting down.", comment: "")
    }
    
    static var thenSlideTheStopButton: String {
        NSLocalizedString("Then, slide the stop button.", bundle: .module, value: "Then, slide the stop button.", comment: "")
    }

    static var tugHintWalkingAid: String {
        NSLocalizedString("You may use a walking aid if needed.", bundle: .module, value: "You may use a walking aid if needed.", comment: "")
    }

    static var tugHintNormalPace: String {
        NSLocalizedString("Perform the test at your normal, safe walking pace.", bundle: .module, value: "Perform the test at your normal, safe walking pace.", comment: "")
    }

    static var tugHintTimeMeasurement: String {
        NSLocalizedString("The time is measured from the moment you stand up from the chair until you sit back down.", bundle: .module, value: "The time is measured from the moment you stand up from the chair until you sit back down.", comment: "")
    }

    static var tug: String {
        NSLocalizedString("TUG", bundle: .module, value: "TUG", comment: "")
    }
    
    static var timedUpAndGo: String {
        NSLocalizedString("Timed Up and Go", bundle: .module, value: "Timed Up and Go", comment: "")
    }

    
    //dualTask
    static var dualTask: String {
        NSLocalizedString("Dual Task", bundle: .module, value: "Dual Task", comment: "")
    }

    static var rollatorOrFourWheeledWalker: String {
        NSLocalizedString("Rollator / four-wheeled walker", bundle: .module, value: "Rollator / four-wheeled walker", comment: "")
    }

    static var cane: String {
        NSLocalizedString("Cane", bundle: .module, value: "Cane", comment: "")
    }
    
    static var current: String {
        NSLocalizedString("Current", bundle: .module, value: "Current", comment: "")
    }
    
    static var previous: String {
        NSLocalizedString("Previous", bundle: .module, value: "Previous", comment: "")
    }

    static var twoCrutches: String {
        NSLocalizedString("2 crutches", bundle: .module, value: "2 crutches", comment: "")
    }

    static var oneCrutch: String {
        NSLocalizedString("1 crutch", bundle: .module, value: "1 crutch", comment: "")
    }

    static var spatialParameters: String {
        NSLocalizedString("Spatial", bundle: .module, value: "Spatial", comment: "")
    }

    static var temporalParameters: String {
        NSLocalizedString("Temporal", bundle: .module, value: "Temporal", comment: "")
    }

    static var rangeOfMotionParameters: String {
        NSLocalizedString("Range of motion", bundle: .module, value: "Range of motion", comment: "")
    }

    static var generalParameters: String {
        NSLocalizedString("General", bundle: .module, value: "General", comment: "")
    }

    static var summary: String {
        NSLocalizedString("Summary", bundle: .module, value: "Summary", comment: "")
    }
    
    static var cancel: String {
        NSLocalizedString("Cancel", bundle: .module, value: "Cancel", comment: "")
    }
    
    static var microphoneAccessTitle: String {
        NSLocalizedString("To perform this test, grant access to the microphone", bundle: .module, value: "To perform this test, grant access to the microphone", comment: "")
    }
    
    static var microphoneAccessSettingsDescription: String {
        NSLocalizedString("Go to your device settings and then toggle on 'Microphone'", bundle: .module, value: "Go to your device settings and then toggle on 'Microphone'", comment: "")
    }
    
    static var skip: String {
        NSLocalizedString("Skip", bundle: .module, value: "Skip", comment: "")
    }
    
    
    
    static var reAnalysisFailed: String {
        NSLocalizedString("Results Not Updated", bundle: .module, value: "Results Not Updated", comment: "")
    }
    

    static var hallwayLengthEditDisabledBanner: String {
        NSLocalizedString("Distance was estimated based on your steps for this test, so the hallway length can't be edited.", bundle: .module, value: "Distance was estimated based on your steps for this test, so the hallway length can't be edited.", comment: "")
    }

    static var gotIt: String {
        NSLocalizedString("Got it!", bundle: .module, value: "Got it!", comment: "")
    }
    
    static var reanalysisFailedDescription: String {
        NSLocalizedString("We couldn't apply your changes. Please try again.", bundle: .module, value: "We couldn't apply your changes. Please try again.", comment: "")
    }
    
    
    
    
    //"Allow access to your microphone."
    
    static var thisApp: String {
        NSLocalizedString("This App", bundle: .module, value: "This App", comment: "")
    }
    
    
    
    
    static func getReadyTextDaulTaskSubtract(number: Int) -> String {
        let string = NSLocalizedString("getReadyTextDaulTaskSubtract", bundle: .module, value: "This activity is 60 seconds long. You will walk at your normal, comfortable pace. At the same time, start counting out loud and clear by subtracting 3 from 111. Keep subtracting 3 from your answer until you're told to stop. Please begin walking and counting when you are ready.", comment: "")
        
        return string.replacingOccurrences(of: "111", with: "\(number)")
    }
    
    static var getReadyTextGeneric: String {
        NSLocalizedString("The recording will begin after the voice instructions", bundle: .module, value: "The recording will begin after the voice instructions", comment: "")
    }
    
    //dualTask instructions
    static var dualTaskInstructions1: String {
        NSLocalizedString("This activity will ask you to walk for 60 seconds at your normal comfortable pace, and at the same time, count backwards by 3 from a number we provide.", bundle: .module, value: "This activity will ask you to walk for 60 seconds at your normal comfortable pace, and at the same time, count backwards by 3 from a number we provide.", comment: "")
    }
    
    static var dualTaskInstructions2: String {
        NSLocalizedString("Please stop immediately if you begin to feel uncomfortable at any time.", bundle: .module, value: "Please stop immediately if you begin to feel uncomfortable at any time.", comment: "")
    }
    
    static var dualTaskInstructions3: String {
        NSLocalizedString("We will tell you when to start and stop.", bundle: .module, value: "We will tell you when to start and stop.", comment: "")
    }
    
    static var dualTaskInstructions4: String {
        NSLocalizedString("Please wear a comfortable pair of walking shoes.", bundle: .module, value: "Please wear a comfortable pair of walking shoes.", comment: "")
    }
    
    static var dualTaskInstructions5: String {
        NSLocalizedString("Find a flat, smooth surface for walking. The straighter this path, the better.", bundle: .module, value: "Find a flat, smooth surface for walking. The straighter this path, the better.", comment: "")
    }
    
    static var dualTaskInstructions6: String {
        NSLocalizedString("Try to walk continuously throughout each trial by turning at the ends of your path, as if you are walking around a cone.", bundle: .module, value: "Try to walk continuously throughout each trial by turning at the ends of your path, as if you are walking around a cone.", comment: "")
    }
    
    static var dualTaskInstructions7: String {
        NSLocalizedString("Importantly, walk at your normal pace. You do not need to walk faster than usual.", bundle: .module, value: "Importantly, walk at your normal pace. You do not need to walk faster than usual.", comment: "")
    }
    
    static var dualTaskInstructions8: String {
        NSLocalizedString("Press on the “Start” button and put the phone in your pocket.", bundle: .module, value: "Press on the “Start” button and put the phone in your pocket.", comment: "")
    }
    
    static var dualTaskInstructions9: String {
        NSLocalizedString("We will verbally guide you through the rest of this activity.", bundle: .module, value: "We will verbally guide you through the rest of this activity.", comment: "")
    }
    
    static var summaryNotice: String {
        NSLocalizedString("Thank you for completing this measurement", bundle: .module, value: "Thank you for completing this measurement", comment: "")
    }
    
    static var noAdditionalInsightsTitle: String {
        NSLocalizedString("No additional insights", bundle: .module, value: "No additional insights", comment: "")
    }
    
    static var noAdditionalInsightsSubtitle: String {
        NSLocalizedString("You did great! We encountered a problem in loading additional insights.", bundle: .module, value: "You did great! We encountered a problem in loading additional insights.", comment: "")
    }
    
    
    //Do any of the following apply to the test you just completed?
    static var doAnyOfTheFollowingApplyToTheTestYouJustCompleted: String {
        NSLocalizedString("Do any of the following apply to the test you just completed?", bundle: .module, value: "Do any of the following apply to the test you just completed?", comment: "")
    }
    
    //The phone rang
    static var thePhoneRang: String {
        NSLocalizedString("The phone rang", bundle: .module, value: "The phone rang", comment: "")
    }
    
    //There were distracting background noises
    static var thereWereDistractingBackgroundNoises: String {
        NSLocalizedString("There were distracting background noises", bundle: .module, value: "There were distracting background noises", comment: "")
    }
    
    //I slipped or tripped
    static var iSlippedOrTripped: String {
        NSLocalizedString("I slipped or tripped", bundle: .module, value: "I slipped or tripped", comment: "")
    }
    
    //The surface was uneven
    static var theSurfaceWasUneven: String {
        NSLocalizedString("The surface was uneven", bundle: .module, value: "The surface was uneven", comment: "")
    }
    
    //The room was relativity dark
    static var theRoomWasRelativelyDark: String {
        NSLocalizedString("The room was relatively dark", bundle: .module, value: "The room was relatively dark", comment: "")
    }
    
    //No issue (test went smoothly)
    static var noIssueTestWentSmoothly: String {
        NSLocalizedString("No issue (test went smoothly)", bundle: .module, value: "No issue (test went smoothly)", comment: "")
    }

    // Hallway Length Feature
    static var hallwayLengthHeader: String {
        NSLocalizedString("Enter the hallway length", bundle: .module, value: "Enter the hallway length", comment: "")
    }

    static var hallwayLengthSubtitle: String {
        NSLocalizedString("(If the test is done in a straight walkway)", bundle: .module, value: "(If the test is done in a straight walkway)", comment: "")
    }
    

    static var hallwayLengthConfirm: String {
        NSLocalizedString("Continue", bundle: .module, value: "Continue", comment: "")
    }

    static var continueWithoutHallway: String {
        NSLocalizedString("Continue without hallway length", bundle: .module, value: "Continue without hallway length", comment: "")
    }

    static var hallwayLengthError: String {
        NSLocalizedString("Please enter a value between %d and %d %@", bundle: .module, value: "Please enter a value between %d and %d %@", comment: "")
    }

    static var hallwayLabel: String {
        NSLocalizedString("Hallway", bundle: .module, value: "Hallway", comment: "")
    }

    static var metersShort: String {
        NSLocalizedString("m", bundle: .module, value: "m", comment: "")
    }

    static var feetShort: String {
        NSLocalizedString("ft", bundle: .module, value: "ft", comment: "")
    }

    static var shortHallwayLengthTitle: String {
        NSLocalizedString("Short hallway length", bundle: .module, value: "Short hallway length", comment: "")
    }

    static var shortHallwayLengthMessage: String {
        NSLocalizedString("The entered hallway length is below recommended clinical guidelines (%@). A shorter hallway may result in additional turns that can affect clinical interpretation.", bundle: .module, value: "The entered hallway length is below recommended clinical guidelines (%@). A shorter hallway may result in additional turns that can affect clinical interpretation.", comment: "")
    }
    
    static var dataReadyForAnalysis: String {
        NSLocalizedString("Data is ready for analysis", bundle: .module, value: "Data is ready for analysis", comment: "")
    }

    static var startTest: String {
        NSLocalizedString("Start Test", bundle: .module, value: "Start Test", comment: "")
    }

    static var hallwayLength: String {
        NSLocalizedString("Hallway length", bundle: .module, value: "Hallway length", comment: "")
    }
    
    static var editHallwayLength: String {
        NSLocalizedString("Edit Hallway Length", bundle: .module, value: "Edit Hallway Length", comment: "")
    }

    static var continueButton: String {
        NSLocalizedString("Continue", bundle: .module, value: "Continue", comment: "")
    }

    static var dontShowAgain: String {
        NSLocalizedString("Don't show again", bundle: .module, value: "Don't show again", comment: "")
    }

    // MARK: - Partial & Minimal Analysis

    static var partialAnalysisBannerTitle: String {
        NSLocalizedString("This walk was partially analyzed", bundle: .module, value: "This walk was partially analyzed", comment: "")
    }

    static var partialAnalysisBannerMessage: String {
        NSLocalizedString("We couldn't capture enough data to generate a score.", bundle: .module, value: "We couldn't capture enough data to generate a score.", comment: "")
    }

    static var minimalAnalysisBannerTitle: String {
        NSLocalizedString("Limited walk data captured", bundle: .module, value: "Limited walk data captured", comment: "")
    }

    static var minimalAnalysisBannerMessage: String {
        NSLocalizedString("We couldn't record enough data for a full analysis.", bundle: .module, value: "We couldn't record enough data for a full analysis.", comment: "")
    }

    static var learnMore: String {
        NSLocalizedString("Learn More", bundle: .module, value: "Learn More", comment: "")
    }

    static var partialAnalysisLearnMoreBody: String {
        NSLocalizedString("Sometimes we're unable to capture all gait parameters, especially during slow walks, short steps, or when the phone isn't positioned against your thigh.\n\nTo get full results, keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", bundle: .module, value: "Sometimes we're unable to capture all gait parameters, especially during slow walks, short steps, or when the phone isn't positioned against your thigh.\n\nTo get full results, keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", comment: "")
    }

    static var minimalAnalysisLearnMoreBody: String {
        NSLocalizedString("Sometimes we're unable to capture enough gait data, especially during slow walks, short steps, or when the phone isn't positioned against your thigh.\n\nTo get full results, keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", bundle: .module, value: "Sometimes we're unable to capture enough gait data, especially during slow walks, short steps, or when the phone isn't positioned against your thigh.\n\nTo get full results, keep the phone snug in your pants pocket or hold it against the thigh, and walk at least 20 steps in a straight line.", comment: "")
    }

    static var stepsCompleted: String {
        NSLocalizedString("steps completed", bundle: .module, value: "steps completed", comment: "")
    }

    static var minutesWalked: String {
        NSLocalizedString("minutes walked", bundle: .module, value: "minutes walked", comment: "")
    }

    static var insightsErrorMessage: String {
        NSLocalizedString("Highlights couldn't be generated due to a system issue.", bundle: .module, value: "Highlights couldn't be generated due to a system issue.", comment: "")
    }

    static var gaitDataErrorMessage: String {
        NSLocalizedString("Gait data couldn't be generated due to a system issue.", bundle: .module, value: "Gait data couldn't be generated due to a system issue.", comment: "")
    }

    static var fullAnalysisErrorMessage: String {
        NSLocalizedString("The full analysis couldn't be generated due to a system issue.", bundle: .module, value: "The full analysis couldn't be generated due to a system issue.", comment: "")
    }

    static var sessionExpiredTitle: String {
        NSLocalizedString("Session Expired", bundle: .module, value: "Session Expired", comment: "")
    }

    static var sessionExpiredMessage: String {
        NSLocalizedString("Your session has expired. Please close and reopen the app — it will reconnect automatically.", bundle: .module, value: "Your session has expired. Please close and reopen the app — it will reconnect automatically.", comment: "")
    }
}
