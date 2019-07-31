# messages.nl
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Copyright (C) 2015 Universiteit Gent
#
# This file is part of the Rasbeb project, an interactive web
# application for Bebras competitions.
#
# Corresponding author:
#
# Kris Coolsaet
# Department of Applied Mathematics, Computer Science and Statistics
# Ghent University
# Krijgslaan 281-S9
# B-9000 GENT Belgium
#
# The Rasbeb Web Application is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# The Rasbeb Web Application is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with the Rasbeb Web Application (file LICENSE in the
# distribution).  If not, see <http://www.gnu.org/licenses/>.

# Form constraints

constraint.required=Verplicht veld.


# Form errors

error.required=Het veld mag niet leeg zijn.
error.passwords.differ=Beide wachtwoorden moeten dezelfde zijn.
error.extendedemail=Ongeldig e-mailadres
error.chooseone=Je moet een keuze maken

# Other errors

error.first=Er bestaat al een administrator-account
error.login=Aanmelden niet geslaagd, identificatie of wachtwoord ongeldig, of toegang geblokkeerd.
error.password=Wachtwoord ongeldig
error.registration.token=De registratie is niet geslaagd. Misschien heb je te lang gewacht of was er een \
   fout in het e-mailadres dat je hebt ingevuld.
error.reset.token=Het aanpassen van het wachtwoord is niet geslaagd. \
   Misschien heb je te lang gewacht of was er een fout in het e-mailadres dat je hebt ingevuld.

error.answer.count= Aantal moet minstens 1 zijn.
error.answer.range= Antwoord moet in het gebied A-{0} liggen.
error.answer.not.letter= Het antwoord moet een hoofdletter zijn.
error.question.upload=Er is iets fout gegaan bij het uploaden van één van de ZIP-bestanden. Gelieve dit \
                      te controleren en bij te sturen.

# When successful

success.first = Een eerste administrator-account is met succes aangemaakt.
success.logout = Je bent met succes afgemeld.
success.changepass = Je wachtwoord is met succes veranderd.
success.registration.student = Je kan nu inloggen in het systeem maar je leraar/onderwijzer zal je nog \
   verder toegang moeten geven vooraleer je aan de wedstrijd(en) kunt deelnemen
success.registration.teacher = Je kunt vanaf nu inloggen in het systeem.

success.token.sent = Dank u. We hebben een e-mail verstuurd naar het opgegeven adres.

# Main page
caption.logout = Afmelden
message.not.logged.in = Niet aangemeld
bebras.id = Bebras ID


# Email subject headers

mail.noreply.address = Bebras online <no-reply@bebras.be>

mail.registration.send.token = Registratie voor Bebras online
mail.registration.user.exists = Onverwachte registratie voor Bebras online
mail.reset.send.token = Nieuw wachtwoord kiezen


# Contests
contest.forced.closed = Sorry, maar je tijd was op. De proef werd automatisch afgesloten.
contest.forced.just.closed = We hebben je laatste antwoord nog net geregistreerd maar nu moeten we de proef \
   helaas afsluiten - je tijd was op.

# data tables
table.pre.show = Toon
table.pre.rows = rijen
table.post.of = van
table.post.previous = Vorige
table.post.next = Volgende
table.main.empty = Geen resultaten

# spreadsheets
gender.abbrev.male = M
gender.abbrev.female = V
gender.abbrev.null = ?

spreadsheet.invalid.gender = Geslacht ongeldig
spreadsheet.classcode.empty = De klassencode mag niet leeg zijn
spreadsheet.name.empty = De naam mag niet leeg zijn
spreadsheet.firstname.empty = De voornaam mag niet leeg zijn
spreadsheet.gender.empty = Het geslacht mag niet weggelaten worden

spreadsheet.unknown.class = Onbekende klas (voor deze school)

spreadsheet.header.info=##Klas,Naam,Voornaam,E-mailadres,M/V,Bebras ID,Wachtwoord
spreadsheet.header.marks=##Klas,Naam,Voornaam,Score,Max

student.not.created = Kon niet geregistreerd worden

student.wrong.name = E-mailadres of Bebras ID geassocieerd met een andere naam 
student.wrong.email = Fout e-mailadres voor dit Bebras ID
student.wrong.gender = E-mailadres of Bebras ID geassocieerd met een ander geslacht
student.wrong.bebras = Foute Bebras ID voor dit e-mailadres

# Teachers
warning.no.school =  Vooraleer je iets kunt doen op dit platform moet je je eerst als verantwoordelijke \
    registeren voor minstens één school.
info.school.added =  De school werd geregistreerd. Gelieve er klassen en leerlingen aan toe te voegen.
#info.school.added =  De school werd geregistreerd. Ga terug naar de startpagina om klassen en leerlingen \
# te registreren (of blijf op deze pagina en registreer u voor een bijkomende school).
error.no.file.uploaded = Je moet een bestand selecteren vooraleer je op de ''Upload''-knop drukt.

yes = Ja
no = Nee
yesno.true = Ja
yesno.false = Nee

lcstatus.DELETED = ???
lcstatus.CAN_BE_OPENED = Kan geopend worden
lcstatus.BLOCKED = Kan (nog) niet geopend worden
lcstatus.OPEN = Open
lcstatus.CLOSED = Gesloten
lcstatus.PENDING = Wacht op resultaten

