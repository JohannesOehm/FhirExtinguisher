library(fhircrackr)

#create example bundle with multiple entries

bundle <- xml2::read_xml(
  "<Bundle>

      <Patient>
          <id value='id1'/>
          <address>
              <use value='home'/>
              <city value='Amsterdam'/>
              <type value='physical'/>
              <country value='Netherlands'/>
          </address>
          <birthDate value='1992-02-06'/>
      </Patient>

      <Patient>
          <id value='id2'/>
          <address>
              <use value='home'/>
              <city value='Rome'/>
              <type value='physical'/>
              <country value='Italy'/>
          </address>
          <address>
              <use value='work'/>
              <city value='Stockholm'/>
              <type value='postal'/>
              <country value='Sweden'/>
          </address>
          <birthDate value='1980-05-23'/>
      </Patient>

      <Patient>
          <id value='id3'/>
          <address>
              <use value='home'/>
              <city value='Berlin'/>
          </address>
          <address>
              <type value='postal'/>
              <country value='France'/>
          </address>
          <address>
              <use value='work'/>
              <city value='London'/>
              <type value='postal'/>
              <country value='England'/>
          </address>
          <birthDate value='1974-12-25'/>
      </Patient>

  </Bundle>"
)

bundle_list <- list(bundle)

#define design
design <- list(
  Patients = list(resource = "//Patient")
)

#extract data frame
dfs <- fhir_crack(bundles = bundle_list, design = design, sep = " ", brackets = c("[", "]"))

dfs$Patients