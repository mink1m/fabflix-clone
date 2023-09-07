use moviedb;

delimiter //

CREATE PROCEDURE add_movie (
    IN title varchar(100),
    IN year INT,
    IN director varchar(100),
    IN star_name varchar(100),
    IN genre_name varchar(100))
BEGIN

    # variables
    DECLARE star_id_ varchar(10);
    DECLARE movie_id_ varchar(10);
    DECLARE genre_id_ integer;

    # check if movie doesn't exist
    IF( (SELECT count(*) FROM movies AS m WHERE m.title=title AND m.year=year AND m.director=director) > 0 )THEN
        SELECT "movie already exists" AS result;
    ELSE

        # check if star doesn't exist
        IF( (SELECT COUNT(*) FROM stars as s WHERE s.name = star_name) = 0 ) THEN

            SET star_id_ = (
                SELECT CONCAT("nm", s.max_num)
                FROM (SELECT (CAST(SUBSTRING(MAX(st.id), 3) AS SIGNED INTEGER) + 1) as max_num FROM stars as st) as s
            );

            INSERT INTO stars(id, name, birthYear)
                VALUES (star_id_, star_name, null);
        ELSE
            SET star_id_ = (SELECT s.id FROM stars as s WHERE s.name = star_name LIMIT 1);
        END IF;

        # check if genre doesn't exist
        IF ( (SELECT COUNT(*) FROM genres as g WHERE g.name = genre_name) = 0 ) THEN
            INSERT INTO genres(name)
                VALUES (genre_name);
        END IF;
        SET genre_id_ = (SELECT g.id FROM genres as g WHERE g.name = genre_name LIMIT 1);

        # create movie
        INSERT INTO movies(id, title, year, director)
            VALUES ((
                SELECT CONCAT("tt",  REPEAT("0", 7 - LENGTH(m.max_num)),m.max_num)
                FROM (SELECT (CAST(SUBSTRING(MAX(mv.id), 3) AS SIGNED INTEGER) + 1) as max_num FROM movies as mv) as m
                        ), title, year, director);

        SET movie_id_ = (SELECT m.id FROM movies as m WHERE m.title = title AND m.year = year AND m.director = director);

        # update stars_in_movie
        INSERT INTO stars_in_movies(starId, movieId)
            VALUES (star_id_, movie_id_);

        # update genre_in_movie
        INSERT INTO genres_in_movies(genreId, movieId)
            VALUES (genre_id_, movie_id_);

        # success return message
        SELECT CONCAT("succesfull created movie with id, ", movie_id_, " and added star with id, ", star_id_, " and genre with id, ", genre_id_) AS result;

    END IF;

END
//

delimiter ;