package se.bahram.aws.lambda.s3.sns;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class PatientCheckoutEvent {

    private String firstName;
    private String middleName;
    private String lastName;
    private String ssn;
}
