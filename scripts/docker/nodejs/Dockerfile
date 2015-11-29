FROM dockerfile/nodejs:latest
MAINTAINER Nilhcem

EXPOSE 8990

RUN echo "#!/bin/sh" >> /bin/start_server.sh
RUN echo "cd /srv" >> /bin/start_server.sh
RUN echo "npm install" >> /bin/start_server.sh
RUN echo "node server.js" >> /bin/start_server.sh

RUN chmod +x /bin/start_server.sh

CMD ["/bin/start_server.sh"]
