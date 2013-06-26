//
//  GARFirstViewController.m
//  GetARideHack
//
//  Created by Kapur, Anurag on 06/10/2012.
//  Copyright (c) 2012 Kapur, Anurag. All rights reserved.
//

#import "GARFirstViewController.h"

@interface GARFirstViewController ()

- (void)sendCustomTweet:(NSString *)tweet;
- (void)displayText:(NSString *)text;
- (void)twitterStream;

@end

@implementation GARFirstViewController

@synthesize fromLocation, toLocation, timeForRide,twitterConnection, now, later, getARide, outputTextView, datePicker;

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    // This notification is posted when the accounts managed by this account store changed in the database.
    // When you receive this notification, you should refetch all account objects.
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(canTweetStatus) name:ACAccountStoreDidChangeNotification object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)getARide:(id)sender {
    
    NSDate *datePicked = [self.datePicker date];
    NSDateFormatter *f2 = [[NSDateFormatter alloc] init];
    [f2 setDateFormat:@"HHmm"];
    self.timeForRide = [f2 stringFromDate:datePicked];
    NSLog(@"time from picker :: %@",self.timeForRide);
    
    
    NSLog(@"get a ride request %@",fromLocation.text);
    NSLog(@"toText %@",toLocation.text);
    NSLog(@"timeText %@",timeForRide);
    
    NSString *source = [fromLocation.text stringByReplacingOccurrencesOfString:@" " withString:@"_"];
    NSString *destination = [toLocation.text stringByReplacingOccurrencesOfString:@" " withString:@"_"];
    
    NSString *tweet = [NSString stringWithFormat:@"@GetARideHack - I need to get from #%@ to #%@ at #t%@ hrs #HTheH",source, destination, timeForRide];
    NSLog(@"Tweet to be sent :: %@",tweet);
    
    [self sendCustomTweet:tweet];
    [self twitterStream];
    [self.getARide removeFromSuperview];
    [self.datePicker removeFromSuperview];
    self.outputTextView.text = @"Thank You! We will notify you when a ride share match is found";
}

- (IBAction)now:(id)sender {
    [self.now setEnabled: NO];
    
    NSDate *today = [NSDate date];
    
    NSDateFormatter *f2 = [[NSDateFormatter alloc] init];
    [f2 setDateFormat:@"HHmm"];
    self.timeForRide = [f2 stringFromDate:today];
}

- (IBAction)later:(id)sender {
    [self.later setEnabled: NO];
    UIDatePicker *pv = [[UIDatePicker alloc] initWithFrame:CGRectMake(0,150,0,0)];
    pv.datePickerMode = UIDatePickerModeTime;
    [self.now removeFromSuperview];
    [self.later removeFromSuperview];
    [self.view addSubview:pv];
    self.datePicker = pv;
}

- (void)canTweetStatus {
    if ([TWTweetComposeViewController canSendTweet]) {
        //
    }else {
        NSLog(@"App not authorised to tweet");
    }
}

- (void)sendCustomTweet:(NSString *)tweet {
	// Create an account store object.
	ACAccountStore *accountStore = [[ACAccountStore alloc] init];
	
	// Create an account type that ensures Twitter accounts are retrieved.
    ACAccountType *accountType = [accountStore accountTypeWithAccountTypeIdentifier:ACAccountTypeIdentifierTwitter];
	
	// Request access from the user to use their Twitter accounts.
    [accountStore requestAccessToAccountsWithType:accountType withCompletionHandler:^(BOOL granted, NSError *error) {
        if(granted) {
			// Get the list of Twitter accounts.
            NSArray *accountsArray = [accountStore accountsWithAccountType:accountType];
			
			// For the sake of brevity, we'll assume there is only one Twitter account present.
			// You would ideally ask the user which account they want to tweet from, if there is more than one Twitter account present.
			if ([accountsArray count] > 0) {
				// Grab the initial Twitter account to tweet from.
				ACAccount *twitterAccount = [accountsArray objectAtIndex:0];
				
				// Create a request, which in this example, posts a tweet to the user's timeline.
				// This example uses version 1 of the Twitter API.
				// This may need to be changed to whichever version is currently appropriate.
				TWRequest *postRequest = [[TWRequest alloc] initWithURL:[NSURL URLWithString:@"http://api.twitter.com/1/statuses/update.json"] parameters:[NSDictionary dictionaryWithObject:tweet forKey:@"status"] requestMethod:TWRequestMethodPOST];
				
				// Set the account used to post the tweet.
				[postRequest setAccount:twitterAccount];
				
				// Perform the request created above and create a handler block to handle the response.
				[postRequest performRequestWithHandler:^(NSData *responseData, NSHTTPURLResponse *urlResponse, NSError *error) {
					NSString *output = [NSString stringWithFormat:@"HTTP response status: %i", [urlResponse statusCode]];
					[self performSelectorOnMainThread:@selector(displayText:) withObject:output waitUntilDone:NO];
				}];
			}
        }
	}];
}

- (void)displayText:(NSString *)text {
	//self.outputTextView.text = text;
    NSLog(@"tweet post status %@",text);
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch *touch = [[event allTouches] anyObject];
    
    if ([fromLocation isFirstResponder] && [touch view] != fromLocation) {
        [fromLocation resignFirstResponder];
    }
    
    if ([toLocation isFirstResponder] && [touch view] != toLocation) {
        [toLocation resignFirstResponder];
    }
    
    if ([outputTextView isFirstResponder] && [touch view] != outputTextView) {
        [outputTextView resignFirstResponder];
    }
    
    
    [super touchesBegan:touches withEvent:event];
}

- (void)twitterStream {
    NSLog(@"Will establish twitter stream connection");
    
    //First, we need to obtain the account instance for the user's Twitter account
    ACAccountStore *store = [[ACAccountStore alloc] init];
    ACAccountType *twitterAccountType = [store accountTypeWithAccountTypeIdentifier:ACAccountTypeIdentifierTwitter];
    
    //  Request permission from the user to access the available Twitter accounts
    [store requestAccessToAccountsWithType:twitterAccountType
                     withCompletionHandler:^(BOOL granted, NSError *error) {
                         if (!granted) {
                             // The user rejected your request
                             NSLog(@"User rejected access to the account.");
                         }
                         else {
                             // Grab the available accounts
                             NSArray *twitterAccounts = [store accountsWithAccountType:twitterAccountType];
                             if ([twitterAccounts count] > 0) {
                                 // Use the first account for simplicity
                                 ACAccount *account = [twitterAccounts objectAtIndex:0];
                                 NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
                                 [params setObject:@"all" forKey:@"replies"];
                                 [params setObject:@"true" forKey:@"stall_warnings"];
                                 //set any other criteria to track
                                 //params setObject:@"words, to, track" forKey@"track"];
                                 
                                 //  The endpoint that we wish to call
                                 NSURL *url = [NSURL URLWithString:@"https://userstream.twitter.com/1.1/user.json"];
                                 
                                 //  Build the request with our parameter
                                 TWRequest *request = [[TWRequest alloc] initWithURL:url
                                                                          parameters:params
                                                                       requestMethod:TWRequestMethodPOST];
                                 
                                 // Attach the account object to this request
                                 [request setAccount:account];
                                 NSURLRequest *signedReq = request.signedURLRequest;
                                 
                                 // make the connection, ensuring that it is made on the main runloop
                                 self.twitterConnection = [[NSURLConnection alloc] initWithRequest:signedReq delegate:self startImmediately: NO];
                                 [self.twitterConnection scheduleInRunLoop:[NSRunLoop mainRunLoop]
                                                                   forMode:NSDefaultRunLoopMode];
                                 [self.twitterConnection start];
                                 
                             } // if ([twitterAccounts count] > 0)
                         } // if (granted) 
                     }];
}

- (void)parseTwitterStreamData:(NSData *)data {
    NSString *replyToScreenName;
    NSString *tweet;
    
    @try {
        SBJsonParser* parser = [[SBJsonParser alloc] init];
        NSDictionary* resultDict = [parser objectWithData:data];
        
        replyToScreenName = [resultDict objectForKey:@"in_reply_to_screen_name"];
        
        if ([replyToScreenName isEqualToString:@"GetARideHack"]) {
            tweet = [resultDict objectForKey:@"text"];
            UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"We Found You A Ride" message:tweet delegate:self cancelButtonTitle:@"Ok" otherButtonTitles:nil, nil];
            [alert show];
            NSLog(@"This must be a response :: %@",tweet);
        }
    }
    @catch (NSException *exception) {
        // Todo
    }
    @finally {
        // Todo
    }
    
    NSLog(@"replyToScreenName %@",replyToScreenName);
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
    NSLog(@"Status code :: %d",[httpResponse statusCode]);
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {

    //NSString *dataReceived = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    NSLog(@"------");
    NSLog(@"------");
    NSLog(@"------");
    [self parseTwitterStreamData:data];
    //NSLog(@"data received %@",dataReceived);
}

@end