package com.campusguide.campus.council.dto;

import com.campusguide.campus.council.validation.ValidSlug;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCouncilRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Slug must not be blank")
    @ValidSlug
    private String slug;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String logoUrl;

    @Email(message = "Email must be valid")
    private String email;

    private String contactNumber;

    private String facultyAdvisor;

    private Boolean isActive;
}
