FROM python:3.6-slim-stretch

# Install the dependencies.
RUN apt-get update && apt-get upgrade -y && \
  apt-get install -y -qq --no-install-recommends

RUN apt-get install poppler-utils -y

RUN apt-get install wkhtmltopdf -y

#RUN apt-get install software-properties-common -y
#RUN add-apt-repository ppa:deadsnakes/ppa
#RUN apt-get install python3.7.0
RUN python --version

#RUN apt-get install python3.7 -y
RUN apt-get install python-pip -y

RUN pip install virtualenv
# Create a virtualenv for dependencies. This isolates these packages from
# system-level packages.
# Use -p python3 or -p python3.7 to select python version. Default is version 2.
RUN virtualenv /env -p python3


# Setting these environment variables are the same as running
# source /env/bin/activate.
ENV VIRTUAL_ENV /env
ENV PATH /env/bin:$PATH

# Copy the application's requirements.txt and run pip to install all
# dependencies into the virtualenv.
RUN pip install --upgrade pip
ADD requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

# Add the application source code.
ADD . /app

# Run a WSGI server to serve the application. gunicorn must be declared as
# a dependency in requirements.txt.
CMD python /app/api.py