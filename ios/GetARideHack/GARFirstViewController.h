//
//  GARFirstViewController.h
//  GetARideHack
//
//  Created by Kapur, Anurag on 06/10/2012.
//  Copyright (c) 2012 Kapur, Anurag. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Twitter/Twitter.h>
#import <Accounts/Accounts.h>
#import <SBJson/SBJson.h>

@interface GARFirstViewController : UIViewController {

    NSURLConnection  *twitterConnection;
    UIButton *now;
    UIButton *later;
    UIButton *getARide;
    UITextView *outputTextView;
    UIDatePicker *datePicker;
}

@property (strong, nonatomic) IBOutlet UITextField *fromLocation;
@property (strong, nonatomic) IBOutlet UITextField *toLocation;
@property (strong, nonatomic) NSString *timeForRide;
@property (strong, nonatomic) NSURLConnection  *twitterConnection;
@property (strong, nonatomic) IBOutlet UIButton *now;
@property (strong, nonatomic) IBOutlet UIButton *later;
@property (strong, nonatomic) IBOutlet UIButton *getARide;
@property (strong, nonatomic) IBOutlet UITextView *outputTextView;
@property (strong, nonatomic) UIDatePicker *datePicker;

@end
