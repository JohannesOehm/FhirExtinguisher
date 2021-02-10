library(tidyverse)
# data <- read.csv("./r/encounters.csv", encoding = "UTF-8")

# data2 <- data[!is.na(data$"PatID") & !data$PatID == "", ]

# data3 <- unique(data2)

#data <- read.csv("http://localhost:8081/fhir/Patient?_profile=https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient&__limit=5000&__columns=id%3AgetIdPart(Patient.id)%40join(%22%20%22)%2Cgender%3Agender%40join(%22%2C%20%22)%2CbirthDate%3AbirthDate.substring(0%5C%2C4)%40join(%22%2C%20%22)",
#                header=TRUE, encoding="UTF-8",
#                 colClasses = c("id"="character","gender"="factor","birthDate"="character")
#)
# Please note that downloading huge amounts of data might take some time and R might give you a timeout.
# In this case, please use either your browser to download a file or a library like RCurl where timeout is configurable.

data <- read.csv('r/patients.csv')

data$age <- 2021 - data$birthDate
data$ageG <- cut(data$age, breaks = c(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 999))
write.csv('r/patients2.csv')


stratified <- as.data.frame(data %>%
                              group_by(gender, ageG) %>%
                              summarise(count = n()))
write.csv(stratified, "r/patients3.csv")

library(ggplot2)
p1 <- ggplot(stratified, aes(x = ageG, y = count, fill = gender)) +
  geom_bar(data = stratified %>% subset(gender == "female"), stat = "identity") +
  geom_bar(data = stratified %>%
    subset(gender == "male") %>%
    transform(count = count * -1), stat = "identity") +
  coord_flip()

# FhirExtinguisher: http://localhost:8081/fhir/Encounter?_profile=https://www.medizininformatik-initiative.de/fhir/core/StructureDefinition/Encounter/KontaktGesundheitseinrichtung&_count=100&__limit=1000000&__columns=PatientIdentifier%3Asubject.resolve().identifier.where(system%3D%22https%3A%2F%2Fwww.medizininformatik-initiative.de%2Ffhir%2Fcore%2FNamingSystem%2Fpatient-identifier%22).value%40join(%22%2C%20%22)%2Cgender%3Asubject.resolve().gender%40join(%22%2C%20%22)%2Cbirthyear%3Asubject.resolve().birthDate.substring(0%5C%2C4)%20%40join(%22%2C%20%22)%2CAufnahmenummer%3Aidentifier.where(type.coding.code%3D%22VN%22).value%20%40join(%22%2C%20%22)%2CAufnMonat%3Aperiod.start.substring(0%5C%2C7)%40join(%22%2C%20%22)%2CEntlMonat%3Aperiod.end.substring(0%5C%2C7)%40join(%22%2C%20%22)%2Cdiag%3Adiagnosis.condition.resolve()%40explodeLong(ICD%3Acode.coding.where(system%3D%22http%3A%2F%2Ffhir.de%2FCodeSystem%2Fdimdi%2Ficd-10-gm%22).code%2CAlphaID%3Acode.coding.where(system%3D%22http%3A%2F%2Ffhir.de%2FCodeSystem%2Fdimdi%2Falpha-id%22).code%2CText%3Acode.coding.where(system%3D%22http%3A%2F%2Ffhir.de%2FCodeSystem%2Fdimdi%2Falpha-id%22).display)
data <- read.csv('r/encounters.csv')

data$age <- 2021 - data$birthyear
data$ageG <- cut(data$age, breaks = c(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 999))
write.csv('r/encounters2.csv')

stratified <- as.data.frame(data %>%
                              group_by(gender, ageG, cond.ICD) %>%
                              summarise(count = n()))
write.csv(stratified, "r/encounter3.csv", row.names = FALSE)