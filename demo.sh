echo "curl localhost:9000/getScreening/1/14/16"
curl localhost:9000/getScreenings/1/14/16
sleep 3
echo $'\n\n'


echo "curl localhost:9000/chooseScreening/1/3/7"
curl localhost:9000/chooseScreening/1/3/7
sleep 3
echo $'\n\n'


echo "curl -X PUT localhost:9000/makeReservation/1 -H 'Content-Type: application/json' -d '{"reservation":[1,2,12,13,22,23], "nameSurname":["Marek Jóźwiak", "Tomasz Trzaśniewicz", "Weronika Rechlewicz", "Mateusz Szutko", "Stefan Radziejewski", "Maciek Stacharczyk"], "ticketTypes":["adult", "adult", "student", "student", "child", "child"]}'"
curl -X PUT localhost:9000/makeReservation/1 -H 'Content-Type: application/json' -d '{"reservation":[1,2,12,13,22,23], "nameSurname":["Marek Jóźwiak", "Tomasz Trzaśniewicz", "Weronika Rechlewicz", "Mateusz Szutko", "Stefan Radziejewski", "Maciek Stacharczyk"], "ticketTypes":["adult", "adult", "student", "student", "child", "child"]}'
sleep 3
echo $'\n\n'


echo "curl localhost:9000/getScreening/1/14/16"
curl localhost:9000/getScreenings/1/14/16
sleep 3
echo $'\n\n'


echo "curl localhost:9000/chooseScreening/2/3/7"
curl localhost:9000/chooseScreening/2/3/7
sleep 3
echo $'\n\n'


echo "curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[1,2,12,13,22,23], "nameSurname":["Marek Jóźwiak", "Tomasz Trzaśniewicz", "Weronika Rechlewicz", "Mateusz Szutko", "Stefan Radziejewski", "Maciek Stacharczyk"], "ticketTypes":["adult", "adult", "student", "student", "child", "child"]}'"
curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[1,2,12,13,22,23], "nameSurname":["Marek Jóźwiak", "Tomasz Trzaśniewicz", "Weronika Rechlewicz", "Mateusz Szutko", "Stefan Radziejewski", "Maciek Stacharczyk"], "ticketTypes":["adult", "adult", "student", "student", "child", "child"]}'
sleep 3
echo $'\n\n'


echo "curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[3], "nameSurname":["Mare4k"], "ticketTypes":["adult"]}'"
curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[3], "nameSurname":["Mare4k"], "ticketTypes":["adult"]}'
sleep 3
echo $'\n\n'


echo "curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[3], "nameSurname":["Marek Jóźwik"], "ticketTypes":["dorosły"]}'"
curl -X PUT localhost:9000/makeReservation/2 -H 'Content-Type: application/json' -d '{"reservation":[3], "nameSurname":["Marek Jóźwik"], "ticketTypes":["dorosły"]}'
sleep 3
echo $'\n\n'
