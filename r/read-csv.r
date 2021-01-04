# library(parsedate)
library(readr)


setClass("fhirDateTime")
setAs("character", "fhirDateTime", function(from) parse_datetime(from))
setClass("fhirTime")
setAs("character", "fhirTime", function(from) parse_time(from))


data <- read.csv("http://localhost:8080/fhir/Patient?_id=1717852&__limit=50&__columns=id%3AgetIdPart(Patient.id)%40join(%22%20%22)%2Cgender%3Agender%40join(%22%2C%20%22)%2Cbirthdate%3AbirthDate%40join(%22%2C%20%22)%2Cdatetime%3A%5C%402020-01-04T10%3A57%3A57%40join(%22%2C%20%22)%2Ctime%3A%5C%40T12%3A00%3A59%40join(%22%2C%20%22)%2CdateTimeTz%3A%5C%402020-01-01T10%3A57%3A57%2B05%3A00%40join(%22%2C%20%22)",
                 header = TRUE,
                 colClasses = c("id" = "character", "gender" = "factor", "birthdate" = "Date", "datetime" = "fhirDateTime", "time" = "fhirTime", "dateTimeTz" = "fhirDateTime")
);
# TODO Sonderzeichen in Patient http://hapi.fhir.org/baseR4/Patient/1717852
# TODO Teste Date, Time usw. mit Zeitzone
# TODO: how to handle null / N/A values